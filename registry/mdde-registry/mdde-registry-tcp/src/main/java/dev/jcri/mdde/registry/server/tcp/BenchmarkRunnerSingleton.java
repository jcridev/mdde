package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.benchmark.BenchmarkRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class BenchmarkRunnerSingleton {
    private static final Logger logger = LogManager.getLogger(BenchmarkRunnerSingleton.class);

    private static class LazyHolder {
        private static BenchmarkRunnerSingleton _instance = new BenchmarkRunnerSingleton();
    }

    BenchmarkRunnerSingleton() {};

    public static BenchmarkRunnerSingleton getDefaultInstance(){
        return BenchmarkRunnerSingleton.LazyHolder._instance;
    }

    private BenchmarkRunner _benchmarkRunner = null;

    public void initializeBenchmarkRunner(BenchmarkRunner runner){
        Objects.requireNonNull(runner, "Object runner object is not set");
        _benchmarkRunner = runner;
    }

    private void verifyState(){
        if(_benchmarkRunner == null){
            throw new IllegalStateException("Benchmark runner is not initialized");
        }
    }

    public BenchmarkRunner getRunner(){
        return _benchmarkRunner;
    }
}
