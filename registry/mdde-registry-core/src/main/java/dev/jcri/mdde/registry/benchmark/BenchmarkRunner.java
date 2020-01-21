package dev.jcri.mdde.registry.benchmark;

import dev.jcri.mdde.registry.benchmark.cluster.IReadOnlyTupleLocator;
import dev.jcri.mdde.registry.benchmark.cluster.ITupleLocatorFactory;
import dev.jcri.mdde.registry.benchmark.ycsb.EYCSBWorkloadCatalog;
import dev.jcri.mdde.registry.benchmark.ycsb.YCSBRunner;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.benchmark.commands.LocateTuple;
import dev.jcri.mdde.registry.shared.benchmark.responses.TupleLocation;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.store.response.TupleCatalog;
import dev.jcri.mdde.registry.store.IReadCommandHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


public class BenchmarkRunner {
    private final ITupleLocatorFactory _tupleLocatorFactory;
    private final IReadCommandHandler _storeReader;
    private final YCSBRunner _ycsbRunner;

    /**
     * Constructor
     * @param tupleLocatorFactory Factory of the tuple locators used for benchmark runs
     * @param storeReader Implementation of the Registry-store reader
     * @param ycsbRunner YCSB Benchmark runner initialized instance
     */
    public BenchmarkRunner(ITupleLocatorFactory tupleLocatorFactory,
                           IReadCommandHandler storeReader,
                           YCSBRunner ycsbRunner){
        Objects.requireNonNull(tupleLocatorFactory);
        Objects.requireNonNull(storeReader);
        Objects.requireNonNull(ycsbRunner);
        _tupleLocatorFactory = tupleLocatorFactory;
        _storeReader = storeReader;
        _ycsbRunner = ycsbRunner;
    }

    private IReadOnlyTupleLocator _tmpTupleLocator = null;

    /**
     * Make preparations for running a benchmark.
     * @throws MddeRegistryException
     */
    public synchronized void prepareBenchmarkEnvironment()
            throws MddeRegistryException {
        IReadOnlyTupleLocator newLocator = _tupleLocatorFactory.getNewTupleLocator();
        TupleCatalog currentTupleStoreSnapshot = _storeReader.getTupleCatalog();
        newLocator.initializeDataLocator(currentTupleStoreSnapshot);
        _tmpTupleLocator = newLocator;
    }

    /**
     * Call after running a benchmark. It disposes of all resources and memory objects that are needed only at a time
     * of executing the benchmark.
     * @throws IOException
     */
    public synchronized void disposeBenchmarkEnvironment()
            throws IOException {
        try {
            if (_tmpTupleLocator != null) _tmpTupleLocator.close();
        } finally {
            _tmpTupleLocator = null;
        }
    }

    private void verifyState(){
        if(_tmpTupleLocator == null){
            throw new IllegalStateException("Benchmark runner has no initialized benchmark environment. " +
                    "You must call prepareBenchmarkEnvironment() prior executing the benchmark.");
        }
    }

    private final ReentrantLock _benchmarkRunnerLock = new ReentrantLock();
    private EBenchmarkRunStage _currentRunStage = EBenchmarkRunStage.READY;
    private EBenchmarkLoadStage _currentLoadState = EBenchmarkLoadStage.EMPTY;

    public boolean generateData(String workload) {
        var knownWorkload = EYCSBWorkloadCatalog.getWorkloadByTag(workload);
        return generateData(knownWorkload);
    }

    /**
     * LOAD initial data into the data nodes
     * @param workload Selected workload.
     * @return
     */
    public boolean generateData(EYCSBWorkloadCatalog workload){
        _benchmarkRunnerLock.lock();
        try {
            if (_currentLoadState != EBenchmarkLoadStage.EMPTY) {
                throw new IllegalStateException(String.format("Benchmark data load is in incorrect state: %s",
                        _currentLoadState.toString()));
            }
            if (_currentRunStage != EBenchmarkRunStage.READY) {
                throw new IllegalStateException("Benchmark is already being executed");
            }
            _currentLoadState = EBenchmarkLoadStage.LOADING;
        }
        finally {
            _benchmarkRunnerLock.unlock();
        }
        try{
            var ycsbRunOutput = this._ycsbRunner.loadWorkload(workload);
        } catch (IOException e) {
            return false;
        } finally {
            _currentLoadState = EBenchmarkLoadStage.READY;
        }
        return true;
    }

    /**
     * Execute workload RUN.
     * Should always LOAD in data before running the benchmark for the first time
     * @param workload Selected workload. Should be the same one each load at least until the test data is reloaded
     *                 into the activate database.
     * @return Relevant statistics gathered during the benchmark run
     */
    public BenchmarkRunResult executeBenchmark(String workload) {
        var knownWorkload = EYCSBWorkloadCatalog.getWorkloadByTag(workload);
        return executeBenchmark(knownWorkload);
    }
    /**
     * Execute workload RUN.
     * Should always LOAD in data before running the benchmark for the first time
     * @param workload Selected workload. Should be the same one each load at least until the test data is reloaded
     *                 into the activate database.
     * @return Relevant statistics gathered during the benchmark run
     */
    public BenchmarkRunResult executeBenchmark(EYCSBWorkloadCatalog workload){
        _benchmarkRunnerLock.lock();
        try {
            if (_currentLoadState != EBenchmarkLoadStage.READY) {
                throw new IllegalStateException(String.format("Benchmark data load is in incorrect state: %s",
                        _currentLoadState.toString()));
            }
            if (_currentRunStage != EBenchmarkRunStage.READY) {
                throw new IllegalStateException("Benchmark is already being executed");
            }
        }
        finally {
            _benchmarkRunnerLock.unlock();
        }
        _currentRunStage = EBenchmarkRunStage.STARTING;
        try {
            _currentRunStage = EBenchmarkRunStage.RUNNING;
            var ycsbRunOutput = this._ycsbRunner.runWorkload(workload);
            _currentRunStage = EBenchmarkRunStage.FINALIZING;
            var result = new BenchmarkRunResult();
            result.setError(null);
            result.setNodes(null); // TODO: Node statistics
            result.setThroughput(ycsbRunOutput.getThroughput());
            return result;
        }
        catch (Exception ex){
            return new BenchmarkRunResult() {{setError(ex.getMessage());}};
        }
        finally {
            _currentRunStage = EBenchmarkRunStage.READY;
        }
    }


    /**
     * Retrieves node where the requested tuple is located and records the retrieval statistic
     * @param tupleParams Tuple location request argument
     * @return Tuple location result container
     */
    public TupleLocation getTupleLocation(LocateTuple tupleParams){
        verifyState();
        var result = _tmpTupleLocator.getNodeForRead(tupleParams.getTupleId());
        return new TupleLocation(result);
    }

    /**
     * Stages of running the benchmark
     */
    private enum EBenchmarkLoadStage {
        EMPTY("Empty"),
        LOADING("Loading"),
        READY("Ready");

        private String _stage;
        EBenchmarkLoadStage(String stage){
            _stage = stage;
        }

        @Override
        public String toString() {
            return _stage;
        }
    }

    /**
     * Stages of running the benchmark
     */
    private enum EBenchmarkRunStage {
        READY("Ready"),
        STARTING("Starting"),
        RUNNING("Running"),
        FINALIZING("Finalizing");

        private String _stage;
        EBenchmarkRunStage(String stage){
            _stage = stage;
        }

        @Override
        public String toString() {
            return _stage;
        }
    }
}
