package dev.jcri.mdde.registry.data;

import java.util.Set;

/**
 * Interface that should be implemented by the class controlling data located directly on the data nodes
 */
public interface IDataShuffler {
    /**
     * Copy a set of tuples from one data node to another
     * @param sourceNodeId ID of the data node from where the Tuples should be copied
     * @param destinationNodeId ID of the data node where the copy of the Tuples should be paced
     * @param tupleIds IDs of the tuples that are located
     * @return True if all of the specified tuples were copied from one node to another, False otherwise
     */
    ShuffleKeysResult copyTuples(String sourceNodeId,
                       String destinationNodeId,
                       Set<String> tupleIds);

    /**
     * Remove a set of tuples from a node
     * @param nodeId ID of the node from which tuples should be removed
     * @param tupleIds Set of tuple IDs
     * @return True if all of the specified tuples were removed from the node, False otherwise
     */
    ShuffleKeysResult deleteTuples(String nodeId,
                                   Set<String> tupleIds);

    /**
     * Remove all data from the data nodes
     * @return True - data was removed
     */
    boolean flushData();
}
