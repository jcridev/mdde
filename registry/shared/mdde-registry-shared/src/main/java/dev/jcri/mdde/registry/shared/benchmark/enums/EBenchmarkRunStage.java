package dev.jcri.mdde.registry.shared.benchmark.enums;

/**
 * Stages of running the benchmark
 */
public enum EBenchmarkRunStage {
    READY("ready"),
    STARTING("starting"),
    RUNNING("running"),
    FINALIZING("finalizing"),
    DONE("done");

    private String _stage;
    EBenchmarkRunStage(String stage){
        _stage = stage;
    }

    @Override
    public String toString() {
        return _stage;
    }
}
