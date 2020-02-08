package dev.jcri.mdde.registry.benchmark.cluster;

public class InMemoryTupleLocatorFactory implements ITupleLocatorFactory {
    @Override
    public IReadOnlyTupleLocator getNewTupleLocator() {
        return new InMemoryTupleLocator(new DefaultCapacityCounter<String>());
    }
}
