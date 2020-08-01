package dev.jcri.mdde.registry.benchmark.cluster;

import java.io.Closeable;
import java.util.List;

/**
 * Capacity counter interface to be used in tuple locator
 * @param <TNodeId> Type of the node ID object
 */
public interface ICapacityCounter<TNodeId> extends Closeable {
    /**
     * Initializes the current instance of the capacity counter to handle a specific number of nodes.
     * @param orderedNodes Ordered collection of Node IDs. Indexes in this collection will correspond to node index
     *                     arguments used in the reset of the implementation.
     */
    void initialize(List<TNodeId> orderedNodes);

    /**
     * Get current mapping of the nodes to indexes (primarily meant for debugging).
     * @return Array of node IDs.
     */
    TNodeId[] getIndexed();

    /**
     * Increment the current capacity counter for the node at the specified index.
     * @param nodeIndex Index of the node.
     */
    void increment(int nodeIndex);

    /**
     * Decrement the current capacity counter for the node at the specified index.
     * @param nodeIndex Index of the node.
     */
    void decrement(int nodeIndex);

    /**
     * Read the current capacity values for all of the nodes.
     * @return Array of length that equals the number of defined nodes with capacity values for nodes corresponding to
     * node indices.
     */
    Integer[] read();

    /**
     * Read capacity only for the nodes with the specific index.
     * @param nodeIndex One or more node indices.
     * @return Array of length that equals the number of defined nodes with capacity values for nodes corresponding to
     * node indices. Values for nodes that were not passed as arguments will be returned as -1.
     */
    Integer[] read(Integer... nodeIndex);
}
