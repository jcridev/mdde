package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.benchmark.BenchmarkRunner;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.store.exceptions.IllegalRegistryActionException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class RegistryStateCommandHandler {
    private final ReentrantLock _commandExecutionLock = new ReentrantLock();
    private final BenchmarkRunner _benchmarkRunner;
    private final IWriteCommandHandler _writeCommandHandler;
    private final List<DBNetworkNodesConfiguration> _dataNodes;

    private ERegistryState _registryState = ERegistryState.shuffle;

    /**
     * Constructor
     * @param writeCommandHandler Current instance of the write command handler for the registry
     * @param benchmarkRunner Benchmark runner object controlled by this handler
     * @param dataNodes List of all nodes known to this registry (from the config file)
     */
    public RegistryStateCommandHandler(
            IWriteCommandHandler writeCommandHandler,
            BenchmarkRunner benchmarkRunner,
            List<DBNetworkNodesConfiguration> dataNodes){
        Objects.requireNonNull(benchmarkRunner);
        Objects.requireNonNull(writeCommandHandler);
        Objects.requireNonNull(dataNodes);
        _benchmarkRunner = benchmarkRunner;
        _writeCommandHandler = writeCommandHandler;
        _dataNodes = dataNodes;
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
    public synchronized BenchmarkRunResult executeBenchmark(String workloadId){
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
     * Completely reset the environment (including erasing all of the data).
     * Next step after this is loading (generating) data in the environment
     */
    public synchronized boolean reset(){
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.shuffle){
                throw new IllegalStateException("Registry is already in the shuffle mode");
            }
            // Erase all data from the Registry
            _writeCommandHandler.reset();
            // Reset data nodes
            // TODO: Flush data nodes
            // Populate Registry with the nodes based on the configuration file
            var defaultNodesParam = new HashSet<String>();
            for(var node: _dataNodes){
                if(!node.getDefaultNode()){
                    continue;
                }
                if(!defaultNodesParam.add(node.getNodeId())){
                    throw new IllegalArgumentException(String.format("Duplicate node id: %s", node.getNodeId()));
                }
            }
            _writeCommandHandler.populateNodes(defaultNodesParam);
        } catch (WriteOperationException | IllegalRegistryActionException e) {
            e.printStackTrace();
        } finally {
            _commandExecutionLock.unlock();
        }
        return true;
    }

    public synchronized boolean syncRegistryToNodes(){
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.benchmark){
                throw new IllegalStateException("Registry is already in the benchmark mode");
            }
            // TODO: Sync data nodes operations queue to the data nodes
            return false;
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
