package dev.jcri.mdde.registry.benchmark.cluster;

import dev.jcri.mdde.registry.data.exceptions.KeyNotFoundException;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.store.response.TupleCatalog;

import java.io.Closeable;

/**
 * During the benchmark run, this registry plays a role of a cluster controller node, this interface provides an
 * interface allowing location of tuples within the cluster according to our own custom logic for read operations.
 *
 * Synthetic nature of such system allows us to abstract from the real world databases heuristic optimizations and
 * concentrate on the learners performance only.
 */
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
     * When the read operation is complete from a node Id, notify the locator by calling this functions.
     * It can be used for statistics gathering purposes or internal load balancing purposes
     * @param nodeId Node Id
     */
    void notifyReadFinished(String nodeId) throws KeyNotFoundException;

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
