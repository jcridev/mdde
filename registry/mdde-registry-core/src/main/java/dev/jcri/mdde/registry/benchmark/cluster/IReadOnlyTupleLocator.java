package dev.jcri.mdde.registry.benchmark.cluster;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.store.response.TupleCatalog;

import java.io.Closeable;

public interface IReadOnlyTupleLocator extends Closeable {
    /**
     * Get Node Id which is most suitable for reading a tuple from. Even if tuple is replicated on multiple nodes, we
     * should return only one, which is mose suitable. This is point where the mock cluster logic is implemented.
     *
     * Implementation of the method is dependant on the chosen execution strategy.
     *
     * This should be returned to the benchmark runner (YCSB).
     * @param tupleId Tuple Id
     * @return Node Id
     */
    String getNodeForRead(String tupleId);

    /**
     * Data locator is not necessarily relying on the same registry storage implementation but instead might construct
     * it's own read-only representation for the benchmark run.
     *
     * This makes sense for example when used with Redis store due  to the fact Redis processes all requests (read and
     * write) within the same event loop, meaning sequentially. While for benchmark we want a data structure that can be
     * queried concurrently.
     *
     * @param tupleCatalog
     */
    void initializeDataLocator(TupleCatalog tupleCatalog) throws MddeRegistryException;
}
