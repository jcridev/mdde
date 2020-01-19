package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.benchmark.BenchmarkRunner;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class RegistryStateCommandHandler {
    private final ReentrantLock _commandExecutionLock = new ReentrantLock();
    private final BenchmarkRunner _benchmarkRunner;

    private ERegistryState _registryState = ERegistryState.shuffle;

    /**
     * Constructor
     * @param benchmarkRunner Benchmark runner object controlled by this handler
     */
    public RegistryStateCommandHandler(BenchmarkRunner benchmarkRunner){
        Objects.requireNonNull(benchmarkRunner);
        _benchmarkRunner = benchmarkRunner;
    }

    public synchronized void switchToBenchmark() throws MddeRegistryException {
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.benchmark){
                throw new IllegalStateException("Registry is already in the benchmark mode");
            }

            _benchmarkRunner.prepareBenchmarkEnvironment();
        }
        finally {
            _commandExecutionLock.unlock();
        }

    }

    public synchronized void switchToShuffle() throws IOException {
        _commandExecutionLock.lock();
        try {
            if(_registryState ==  ERegistryState.shuffle){
                throw new IllegalStateException("Registry is already in the shuffle mode");
            }

            _benchmarkRunner.disposeBenchmarkEnvironment();
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }


    private enum ERegistryState{
        benchmark,
        shuffle
    }
}
