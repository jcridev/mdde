package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.benchmark.BenchmarkRunner;
import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.data.ShuffleKeysResult;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.store.exceptions.IllegalRegistryModeException;
import dev.jcri.mdde.registry.store.exceptions.RegistryModeAlreadySetException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import dev.jcri.mdde.registry.store.exceptions.action.IllegalRegistryActionException;
import dev.jcri.mdde.registry.store.queue.IDataShuffleQueue;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;
import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;
import dev.jcri.mdde.registry.store.snapshot.FileBasedSnapshotManager;
import dev.jcri.mdde.registry.store.snapshot.StoreSnapshotManagerBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    private final StoreSnapshotManagerBase _snapshotsManager;

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

        _snapshotsManager = new FileBasedSnapshotManager(snapshotsDir, _dataShuffler, _registryStoreManager);
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
     * @return Current status of the benchmark run
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
            final String defaultSnapshotId = _snapshotsManager.getDefaultSnapshotId();
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
                return _snapshotsManager.loadSnapshot(defaultSnapshotId);
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
     * @return True - flush all was a success
     */
    public synchronized boolean flushAll() throws MddeRegistryException, IOException {
        _commandExecutionLock.lock();
        try {
            if (_registryState != ERegistryState.shuffle) {
                throw new IllegalRegistryModeException(_registryState, ERegistryState.shuffle);
            }

            _dataShuffler.flushData();
            _registryStoreManager.flushAllData();
            _benchmarkRunner.flushData();
            _snapshotsManager.flushSnapshots();
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
     * Crete full snapshot of the Registry with Data and dump them in the snapshots directory
     * @param isDefault If True - newly created snapshot is set as Default and will be used during the RESET execution
     * @return
     * @throws IOException
     */
    public String createSnapshot(boolean isDefault) throws IOException {
        _commandExecutionLock.lock();
        try {
            return _snapshotsManager.createSnapshot(isDefault);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Load snapshot from a file
     * @param snapshotId ID of the snapshot to load
     * @return True - snapshot was restored successfully
     * @throws IOException
     */
    public boolean loadSnapshot(String snapshotId) throws IOException {
        _commandExecutionLock.lock();
        try {
            return _snapshotsManager.loadSnapshot(snapshotId);
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
            if(_registryState == ERegistryState.benchmark){
                throw new IllegalStateException("Registry is in the benchmark mode");
            }
            logger.info("Starting the data shuffle queue execution.");
            while(!_dataShuffleQueue.isEmpty()){
                var nextAction = _dataShuffleQueue.poll();
                ShuffleKeysResult shuffleResult = null;
                switch (nextAction.getActionType()){
                    case DELETE:
                        DataDeleteAction delAction = (DataDeleteAction) nextAction;
                        logger.info("Executing DELETE action from queue: Del '{}' from node '{}'",
                                String.join(";", delAction.getTupleIds()),delAction.getDataNode());
                        shuffleResult = _dataShuffler.deleteTuples(delAction.getDataNode(), delAction.getTupleIds());
                        break;
                    case COPY:
                        DataCopyAction cpyAction = (DataCopyAction) nextAction;
                        logger.info("Executing COPY action from queue: Copy '{}' from node '{}' to node '{}'",
                                String.join(";", cpyAction.getTupleIds()),
                                cpyAction.getSourceNode(),
                                cpyAction.getDestinationNode());
                        shuffleResult= _dataShuffler.copyTuples(cpyAction.getSourceNode(),
                                                                cpyAction.getDestinationNode(),
                                                                cpyAction.getTupleIds());
                        break;
                    default:
                        throw new RuntimeException(String.format("Unknown data type returned from the queue: %s",
                                                                nextAction.getActionType().getTag()));
                }
                if(shuffleResult.getError() != null){
                    throw shuffleResult.getError();
                }
            }
            logger.info("Finished the data shuffle queue execution.");
            return true;
        }
        catch (IOException ex){
            logger.error("Failed during synchronization of registry and data nodes", ex);
            throw ex;
        } catch (Throwable e) {
            logger.error("Failed during synchronization of registry and data nodes", e);
            throw new IOException(e);
        } finally {
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
