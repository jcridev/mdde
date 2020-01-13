package dev.jcri.mdde.registry.benchmark;

import dev.jcri.mdde.registry.benchmark.cluster.ITupleLocatorFactory;
import dev.jcri.mdde.registry.configuration.RegistryConfig;

import java.util.Objects;

/**
 *
 * @param <TStore> Registry back storage configuration
 */
public class BenchmarkRunner<TStore> {
    private final RegistryConfig<TStore>  _mddeRegistryConfig;
    private final ITupleLocatorFactory _tupleLocatorFactory;

    public BenchmarkRunner(RegistryConfig<TStore> config, ITupleLocatorFactory tupleLocatorFactory){
        Objects.requireNonNull(config);
        Objects.requireNonNull(tupleLocatorFactory);
        _mddeRegistryConfig = config;
        _tupleLocatorFactory = tupleLocatorFactory;
    }

    public void ExecuteBenchmarkRun(){

    }
}
