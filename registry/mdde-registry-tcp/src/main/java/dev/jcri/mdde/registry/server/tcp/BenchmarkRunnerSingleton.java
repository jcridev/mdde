package dev.jcri.mdde.registry.server.tcp;

public class BenchmarkRunnerSingleton {
    private static class LazyHolder {
        private static BenchmarkRunnerSingleton _instance = new BenchmarkRunnerSingleton();
    }

    BenchmarkRunnerSingleton() {};
}
