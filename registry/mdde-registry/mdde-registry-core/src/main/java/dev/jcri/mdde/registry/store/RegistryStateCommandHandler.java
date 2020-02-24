package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.benchmark.BenchmarkRunner;
import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.store.exceptions.IllegalRegistryActionException;
import dev.jcri.mdde.registry.store.exceptions.IllegalRegistryModeException;
import dev.jcri.mdde.registry.store.exceptions.RegistryModeAlreadySetException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import dev.jcri.mdde.registry.store.queue.IDataShuffleQueue;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;
import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class RegistryStateCommandHandler {
    private static final Logger logger = LogManager.getLogger(RegistryStateCommandHandler.class);

    private final ReentrantLock _commandExecutionLock = new ReentrantLock();
    private final BenchmarkRunner _benchmarkRunner;
    private final IWriteCommandHandler _writeCommandHandler;
    private final List<DBNetworkNodesConfiguration> _dataNodes;
    private final IDataShuffler _dataShuffler;
    private final IDataShuffleQueue _dataShuffleQueue;
    private final IStoreManager _registryStoreManager;
    private final String _snapshotsDirectory;

    private ERegistryState _registryState = ERegistryState.shuffle;

    /**
     * Constructor
     * @param writeCommandHandler Current instance of the write command handler for the registry
     * @param registryStoreManager Implementation of the management functions for the registry store
     * @param nodeDataShuffler Implementation of the data nodes controller
     * @param benchmarkRunner Benchmark runner object controlled by this handler
     * @param dataNodes List of all nodes known to this registry (from the config file)
     */
    public RegistryStateCommandHandler(
            IWriteCommandHandler writeCommandHandler,
            IStoreManager registryStoreManager,
            IDataShuffler nodeDataShuffler,
            IDataShuffleQueue nodeDataShuffleQueue,
            BenchmarkRunner benchmarkRunner,
            List<DBNetworkNodesConfiguration> dataNodes,
            String snapshotsDir){
        Objects.requireNonNull(benchmarkRunner);
        Objects.requireNonNull(writeCommandHandler);
        Objects.requireNonNull(nodeDataShuffler);
        Objects.requireNonNull(nodeDataShuffleQueue);
        Objects.requireNonNull(registryStoreManager);
        Objects.requireNonNull(dataNodes);

        if(snapshotsDir == null || snapshotsDir.isBlank()){
            throw new IllegalArgumentException("Path to the snapshots directory can't be null or empty");
        }

        _benchmarkRunner = benchmarkRunner;
        _writeCommandHandler = writeCommandHandler;
        _dataNodes = dataNodes;
        _dataShuffler = nodeDataShuffler;
        _dataShuffleQueue = nodeDataShuffleQueue;
        _registryStoreManager = registryStoreManager;
        _snapshotsDirectory = snapshotsDir;
    }

    /**
     * Prepare the registry to run benchmarks
     * @throws MddeRegistryException
     */
    public synchronized boolean switchToBenchmark() throws MddeRegistryException {
        _commandExecutionLock.lock();
        logger.trace("Switching to benchmark mode");
        try {
            if(_registryState ==  ERegistryState.benchmark){
                throw new RegistryModeAlreadySetException("Registry is already in the benchmark mode");
            }
            _benchmarkRunner.prepareBenchmarkEnvironment();
            logger.trace("Benchmark environment was prepared");
            _registryState = ERegistryState.benchmark;
            return true;
        }
        catch (Exception ex){
            logger.error("Failed switching to benchmark mode", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Prepare the registry for data shuffling
     * @throws IOException
     */
    public synchronized boolean switchToShuffle() throws IOException, MddeRegistryException {
        _commandExecutionLock.lock();
        logger.trace("Switching to shuffle mode");
        try {
            if(_registryState == ERegistryState.shuffle){
                throw new RegistryModeAlreadySetException("Registry is already in the shuffle mode");
            }
            _benchmarkRunner.disposeBenchmarkEnvironment();
            _registryState = ERegistryState.shuffle;
            return true;
        } catch (Exception ex){
            logger.error("Failed switching to shuffle mode", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Load data into the environment
     * @return
     */
    public synchronized boolean generateData(String workloadId) throws MddeRegistryException {
        _commandExecutionLock.lock();
        logger.trace("Generate data was called");
        try {
            if(_registryState == ERegistryState.shuffle){
                logger.warn("Registry is in the shuffle mode, unable to execute data generation");
                throw new IllegalRegistryModeException(_registryState, ERegistryState.benchmark);
            }
            return _benchmarkRunner.generateData(workloadId);
        }
        catch (Exception ex){
            logger.error("Failed generating data", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Pre-populate default nodes in the registry
     */
    public synchronized Set<String> initializeDefaultNodes()
            throws MddeRegistryException {
        _commandExecutionLock.lock();
        try {
            var defaultNodesParam = new HashSet<String>();
            for(var node: _dataNodes){
                if(!node.getDefaultNode()){
                    continue;
                }
                if(!defaultNodesParam.add(node.getNodeId())){
                    throw new IllegalArgumentException(String.format("Duplicate node id: %s", node.getNodeId()));
                }
            }

            if(defaultNodesParam.size() == 0){
                logger.info("No default nodes to initialize");
                return defaultNodesParam;
            }
            var success = _writeCommandHandler.populateNodes(defaultNodesParam);
            logger.info(MessageFormat.format("Default nodes initialized: {0}", success));
            if(success) {
                logger.info(MessageFormat.format("Initialized default nodes: {0}", String.join(",", defaultNodesParam)));
            }
            return defaultNodesParam;
        }
        catch (Exception ex){
            logger.error("Failed initializing default nodes", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Run benchmark within the prepared environment and return the results
     * @return
     */
    public synchronized String executeBenchmark(String workloadId) throws MddeRegistryException {
        _commandExecutionLock.lock();
        logger.trace("Benchmark execution was called");
        try {
            if(_registryState == ERegistryState.shuffle){
                logger.warn("Registry is in the shuffle mode, unable to execute benchmark");
                throw new IllegalRegistryModeException(_registryState, ERegistryState.benchmark);
            }
            return _benchmarkRunner.executeBenchmark(workloadId);
        }
        catch (Exception ex){
            logger.error("Failed executing benchmark", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Retrieve the status of the latest executed or currently executing benchmark
     * @return
     */
    public synchronized BenchmarkStatus retrieveLatestBenchmarkRunStatus(){
        return _benchmarkRunner.getBenchmarkStatus();
    }

    /**
     * Completely reset the environment (including erasing all of the data).
     * Next step after this is loading (generating) data in the environment
     */
    public synchronized boolean reset() throws IOException, MddeRegistryException{
        _commandExecutionLock.lock();
        try {
            if(_registryState != ERegistryState.benchmark){
                throw new IllegalRegistryModeException(_registryState, ERegistryState.benchmark);
            }
            final String defaultSnapshotId = getDefaultSnapshotId();
            // Flush registry
            _writeCommandHandler.flush();
            // Flush data nodes
            _dataShuffler.flushData();
            _dataShuffleQueue.clear();

            if(defaultSnapshotId == null){
                // Populate only nodes from the configuration file (default initialization of the empty registry)
                var defaultNodesParam = new HashSet<String>();
                for(var node: _dataNodes){
                    if(!node.getDefaultNode()){
                        continue;
                    }
                    if(!defaultNodesParam.add(node.getNodeId())){
                        throw new IllegalArgumentException(String.format("Duplicate node id: %s", node.getNodeId()));
                    }
                }
                return _writeCommandHandler.populateNodes(defaultNodesParam);
            }
            else{
                return loadSnapshot(defaultSnapshotId);
            }
        } catch (WriteOperationException | IllegalRegistryActionException e) {
            logger.error("Failed flushing all", e);
            throw new IOException(e);
        }
        catch (IOException e){
            logger.error("Failed flushing all", e);
            throw e;
        } finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Completely erase all records from the Registry and from the Data nodes
     * @return
     */
    public synchronized boolean flushAll() throws MddeRegistryException {
        _commandExecutionLock.lock();
        try {
            if (_registryState != ERegistryState.shuffle) {
                throw new IllegalRegistryModeException(_registryState, ERegistryState.shuffle);
            }

            _dataShuffler.flushData();
            _registryStoreManager.flushAllData();
            _benchmarkRunner.flushData();
            return true;
        }
        catch (Exception ex){
            logger.error("Failed flushing all", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Execute the Data shuffler queue
     * @return True if the queue was executed
     * @throws IOException
     */
    public synchronized boolean syncRegistryToNodes() throws IOException {
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.benchmark){
                throw new IllegalStateException("Registry is already in the benchmark mode");
            }
            while(_dataShuffleQueue.isEmpty()){
                var nextAction = _dataShuffleQueue.poll();
                switch (nextAction.getActionType()){
                    case DELETE:
                        DataDeleteAction delAction = (DataDeleteAction) nextAction;
                        _dataShuffler.deleteTuples(delAction.getDataNode(), delAction.getTupleIds());
                        break;
                    case COPY:
                        DataCopyAction cpyAction = (DataCopyAction) nextAction;
                        _dataShuffler.copyTuples(cpyAction.getSourceNode(),
                                                    cpyAction.getDestinationNode(),
                                                    cpyAction.getTupleIds());
                        break;
                    default:
                        throw new RuntimeException(String.format("Unknown data type returned from the queue: %s",
                                                                nextAction.getActionType().getTag()));
                }
            }
            return true;
        }
        catch (Exception ex){
            logger.error("Failed during synchronization of registry and data nodes", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Postfix for the registry records snapshot file
     */
    private static final String _registryDumpFilePostfix = "-registry.dmp";
    /**
     * Postfix for the data nodes data snapshot file
     */
    private static final String _dataNodeDumpFilePostfix = "-data.dmp";


    private static final String _snapshotsCatalogFileName = "mdde-snapshots.db";

    /**
     * Get fully resolved, normalized path to the snapshots directory. If the directory doesn't exist, it will be created.
     * @return fully resolved, normalized path to the snapshots directory
     * @throws NotDirectoryException Thrown if there is a file with the same name as the configured snapshots directory
     */
    private String getSnapshotsDirPath() throws NotDirectoryException {
        var normalizedPath = Paths.get(_snapshotsDirectory).toAbsolutePath().normalize().toString();
        var snapDirFile = new File(normalizedPath);
        if (snapDirFile.exists() && snapDirFile.isFile()) {
            throw new NotDirectoryException(normalizedPath);
        }
        boolean mkdirsRes = snapDirFile.mkdirs();
        logger.trace("Create snapshot, snapshot directory created: {}", mkdirsRes);
        return normalizedPath;
    }

    private String getSnapshotsDbPath() throws NotDirectoryException {
        final String snapshotsPath = getSnapshotsDirPath();
        return Paths.get(snapshotsPath, _snapshotsCatalogFileName).toString();
    }

    /**
     * Get connection to the SQLLite catalog of the snapshots
     * @return SQLite connection
     * @throws SQLException
     * @throws NotDirectoryException
     */
    private Connection getSnapshotsCatalogConnection()
            throws SQLException, NotDirectoryException {
        var pathToDb = getSnapshotsDbPath();
        var fileAlreadyExists = new File(pathToDb).exists();
        var connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", pathToDb));
        if(fileAlreadyExists){
            return connection;
        }
        else{
            var createSnapsTable =  "CREATE TABLE IF NOT EXISTS snapshots (\n" +
                                    "    snapshot_id TEXT NOT NULL PRIMARY KEY,\n" +
                                    "    created DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')) NOT NULL\n" +
                                    ");";

            var createDefSnTable =  "CREATE TABLE IF NOT EXISTS default_snapshot (\n" +
                                    "    id INTEGER PRIMARY KEY CHECK (id = 0),\n" +
                                    "    snapshot_id TEXT NOT NULL,\n" +
                                    "    FOREIGN KEY (snapshot_id)\n" +
                                    "       REFERENCES snapshots (snapshot_id)\n" +
                                    ");";

            try(Statement statement = connection.createStatement()) {
                statement.executeUpdate(createSnapsTable);
                statement.executeUpdate(createDefSnTable);
            }
        }
        return connection;
    }

    private void insertNewSnapshotId(String snapshotId, boolean asDefault)
            throws SQLException, NotDirectoryException {
        try(var connection = getSnapshotsCatalogConnection()){

            var newSnapIdInsert = String.format("INSERT INTO snapshots(snapshot_id) VALUES ('%s');", snapshotId);
            Statement statement = connection.createStatement();
            statement.executeUpdate(newSnapIdInsert);
            if(asDefault){
                var newDefSnapIdInsert =
                        String.format(  "INSERT INTO default_snapshot(id, snapshot_id)\n" +
                                        "VALUES (0, '%s')\n" +
                                        "ON CONFLICT(id)\n" +
                                        "    DO UPDATE SET snapshot_id=excluded.snapshot_id;", snapshotId);
                statement.executeUpdate(newDefSnapIdInsert);
            }
        }
    }

    private String getDefaultSnapshotId() throws IOException {
        try(var connection = getSnapshotsCatalogConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT snapshot_id FROM default_snapshot WHERE id=0;");
            if(rs.next()){
                return rs.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            var errorText = "Error accessing snapshots db";
            logger.error(errorText, e);
            throw new IOException(errorText, e);
        }
    }

    private boolean flushSnapshots() throws IOException, SQLException {
        var pathToDb = getSnapshotsDbPath();
        if(!new File(pathToDb).exists()){
            return true;
        }
        // Delete snapshot files
        try(var connection = getSnapshotsCatalogConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT snapshot_id FROM snapshots;");
            final String snapshotsPath = getSnapshotsDirPath();
            while(rs.next())
            { // Remove known snapshot files
                try{
                    var snapFilenamePrefix = rs.getString(1);
                    var pathRegistry = Paths.get(snapshotsPath, snapFilenamePrefix + _registryDumpFilePostfix);
                    var pathData = Paths.get(snapshotsPath, snapFilenamePrefix + _dataNodeDumpFilePostfix);
                    Files.deleteIfExists(pathRegistry);
                    Files.deleteIfExists(pathData);
                } catch (IOException e) {
                    logger.error("Error flushing a snapshot", e);
                }
            }
        }
        // Delete the database
        Files.deleteIfExists(Paths.get(pathToDb));
        return true;
    }

    /**
     * Crete full snapshot of the Registry with Data and dump them in the snapshots directory
     * @param isDefault If True - newly created snapshot is set as Default and will be used during the RESET execution
     * @return
     * @throws IOException
     */
    public synchronized String createSnapshot(boolean isDefault) throws IOException {
        _commandExecutionLock.lock();
        try {
            final String snapshotsPath = getSnapshotsDirPath();
            // Generate snapshot ID (used as filenames)
            final String snapFilenamePrefix = UUID.randomUUID().toString().replace("-", "");
            final String snapRegistryFile = Paths.get(snapshotsPath, snapFilenamePrefix + _registryDumpFilePostfix)
                    .toString();
            final String snapDataNodesFile = Paths.get(snapshotsPath, snapFilenamePrefix + _dataNodeDumpFilePostfix)
                    .toString();
            // Dump registry into the file
            _registryStoreManager.dumpToFile(snapRegistryFile, true);
            // Dump data from all of the nodes into the file
            _dataShuffler.dumpToFile(snapDataNodesFile, true);
            // Record the snapshot in the catalog
            insertNewSnapshotId(snapFilenamePrefix, isDefault);

            return snapFilenamePrefix;
        }
        catch (SQLException e){
            var errorText = "Failed to save info about a new snapshot";
            logger.error(errorText, e);
            throw new IOException(errorText, e);
        }
        catch (Exception ex){
            logger.error("Failed creating snapshot", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Load snapshot from a file
     * @param snapshotId ID of the snapshot to load
     * @return
     * @throws IOException
     */
    public synchronized boolean loadSnapshot(String snapshotId) throws IOException{
        _commandExecutionLock.lock();
        try {
            if (snapshotId == null || snapshotId.isBlank()) {
                throw new IllegalArgumentException("Snapshot ID is not set");
            }
            final String snapshotsPath = getSnapshotsDirPath();
            var snapDirFile = new File(snapshotsPath);
            if (!snapDirFile.exists() || snapDirFile.isFile()) {
                throw new NotDirectoryException(snapshotsPath);
            }
            final String snapRegistryFile = Paths.get(snapshotsPath, snapshotId + _registryDumpFilePostfix)
                    .toString();
            final String snapDataNodesFile = Paths.get(snapshotsPath, snapshotId + _dataNodeDumpFilePostfix)
                    .toString();

            if (!new File(snapRegistryFile).isFile()) {
                throw new FileNotFoundException(snapRegistryFile);
            }
            if (!new File(snapDataNodesFile).isFile()) {
                throw new FileNotFoundException(snapDataNodesFile);
            }

            var dataRestored = _dataShuffler.restoreFromFile(snapDataNodesFile);
            var registryRestored = _registryStoreManager.restoreFromFile(snapRegistryFile);

            return dataRestored && registryRestored;
        }
        catch (Exception ex){
            logger.error("Failed loading snapshot", ex);
            throw ex;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }


    /**
     * Get the current state of the Registry
     * @return ERegistryState
     */
    public ERegistryState getCurrentState(){
        return _registryState;
    }

    /**
     * Possible states of the registry
     */
    public enum ERegistryState{
        /**
         * In this state the Registry is not accepting any write commands and can run benchmark against the data
         * distribution
         */
        benchmark,
        /**
         * Perform reads and writes but can't run benchmark commands
         */
        shuffle
    }
}
