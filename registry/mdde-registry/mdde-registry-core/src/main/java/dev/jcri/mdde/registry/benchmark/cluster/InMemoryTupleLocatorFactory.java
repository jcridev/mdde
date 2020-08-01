package dev.jcri.mdde.registry.benchmark.cluster;

/**
 * Factory class for the In-memory tuple locator used during the benchmark run.
 */
public class InMemoryTupleLocatorFactory implements ITupleLocatorFactory {

    /**
     * Create a new instances of the In-memory tuple locator based on the current allocation of data records. Must be
     * re-created after any changes in the allocation of data.
     * @return InMemoryTupleLocator instance.
     */
    @Override
    public IReadOnlyTupleLocator getNewTupleLocator() {
        return new InMemoryTupleLocator(new DefaultCapacityCounter<String>());
    }
}
