package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.benchmark.BenchmarkRunner;
import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.store.exceptions.IllegalRegistryActionException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import dev.jcri.mdde.registry.store.queue.IDataShuffleQueue;
import dev.jcri.mdde.registry.store.queue.actions.DataAction;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;
import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;

import javax.lang.model.element.UnknownElementException;
import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public final class RegistryStateCommandHandler {
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
        try {
            if(_registryState ==  ERegistryState.benchmark){
                throw new IllegalStateException("Registry is already in the benchmark mode");
            }
            _benchmarkRunner.prepareBenchmarkEnvironment();
            return true;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Prepare the registry for data shuffling
     * @throws IOException
     */
    public synchronized boolean switchToShuffle() throws IOException {
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.shuffle){
                throw new IllegalStateException("Registry is already in the shuffle mode");
            }
            _benchmarkRunner.disposeBenchmarkEnvironment();
            return true;
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Load data into the environment
     * @return
     */
    public synchronized boolean generateData(String workloadId){
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.shuffle){
                throw new IllegalStateException("Registry is already in the shuffle mode");
            }
            return _benchmarkRunner.generateData(workloadId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Run benchmark within the prepared environment and return the results
     * @return
     */
    public synchronized String executeBenchmark(String workloadId){
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.shuffle){
                throw new IllegalStateException("Registry is already in the shuffle mode");
            }
            return _benchmarkRunner.executeBenchmark(workloadId);
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
    public synchronized boolean reset() throws IOException{
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.shuffle){
                throw new IllegalStateException("Registry is already in the shuffle mode");
            }
            final String defaultSnapshotId = _registryStoreManager.getDefaultSnapshotId();
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
            e.printStackTrace();
        } finally {
            _commandExecutionLock.unlock();
        }
        return false;
    }

    /**
     * Completely erase all records from the Registry and from the Data nodes
     * @return
     */
    public synchronized boolean flushAll(){
        if(_registryState !=  ERegistryState.shuffle){
            throw new IllegalStateException("Registry is must be in a shuffle mode to execute FLUSH");
        }

        _dataShuffler.flushData();
        _registryStoreManager.eraseAllData();
        return true;
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

    /**
     * Crete full snapshot of the Registry with Data and dump them in the snapshots directory
     * @param isDefault If True - newly created snapshot is set as Default and will be used during the RESET execution
     * @return
     * @throws IOException
     */
    public synchronized String createSnapshot(boolean isDefault) throws IOException {
        // First check if the snapshots dir exits
        var snapDirFile = new File(_snapshotsDirectory);
        if(snapDirFile.exists() && snapDirFile.isFile()){
            throw new NotDirectoryException(_snapshotsDirectory);
        }
        snapDirFile.mkdirs();
        // Generate snapshot ID (used as filenames)
        final String snapFilenamePrefix = UUID.randomUUID().toString().replace("-", "");
        final String snapRegistryFile = Paths.get(_snapshotsDirectory, snapFilenamePrefix + _registryDumpFilePostfix)
                                        .toAbsolutePath().normalize().toString();
        final String snapDataNodesFile =  Paths.get(_snapshotsDirectory, snapFilenamePrefix + _dataNodeDumpFilePostfix)
                                        .toAbsolutePath().normalize().toString();

        _registryStoreManager.dumpToFile(snapRegistryFile, true);
        _dataShuffler.dumpToFile(snapDataNodesFile, true);
        if(isDefault){
            _registryStoreManager.assignDefaultSnapshot(snapFilenamePrefix);
        }

        return snapFilenamePrefix;
    }

    /**
     * Load snapshot from a file
     * @param snapshotId ID of the snapshot to load
     * @return
     * @throws IOException
     */
    public synchronized boolean loadSnapshot(String snapshotId) throws IOException{
        if(snapshotId == null || snapshotId.isBlank()){
            throw new IllegalArgumentException("Snapshot ID is not set");
        }
        var snapDirFile = new File(_snapshotsDirectory);
        if(!snapDirFile.exists() || snapDirFile.isFile()){
            throw new NotDirectoryException(_snapshotsDirectory);
        }
        final String snapRegistryFile = Paths.get(_snapshotsDirectory, snapshotId + _registryDumpFilePostfix)
                .toAbsolutePath().normalize().toString();
        final String snapDataNodesFile =  Paths.get(_snapshotsDirectory, snapshotId + _dataNodeDumpFilePostfix)
                .toAbsolutePath().normalize().toString();

        if(!new File(snapRegistryFile).isFile()){
            throw new FileNotFoundException(snapRegistryFile);
        }
        if(!new File(snapDataNodesFile).isFile()){
            throw new FileNotFoundException(snapDataNodesFile);
        }

        var dataRestored = _dataShuffler.restoreFromFile(snapDataNodesFile);
        var registryRestored = _registryStoreManager.restoreFromFile(snapRegistryFile);

        return dataRestored && registryRestored;
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
