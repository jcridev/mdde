package dev.jcri.mdde.registry.data;

import dev.jcri.mdde.registry.store.queue.actions.DataAction;

import java.io.IOException;
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
     * @return Execution result
     */
    ShuffleKeysResult copyTuples(String sourceNodeId,
                       String destinationNodeId,
                       Set<String> tupleIds);

    /**
     * Remove a set of tuples from a node
     * @param nodeId ID of the node from which tuples should be removed
     * @param tupleIds Set of tuple IDs
     * @return Execution result
     */
    ShuffleKeysResult deleteTuples(String nodeId,
                                   Set<String> tupleIds);


    /**
     * Remove all data from the data nodes
     * @return True - data was removed
     */
    boolean flushData();

    /**
     * Save all of the current data nodes data snapshots to a file.
     * Database specific file, meant for later restore by restoreFromFile(...)
     * @param pathToFile Full file name
     * @param overwrite Overwrite if the file already exits
     * @return True - data was saved successfully
     */
    boolean dumpToFile(String pathToFile, boolean overwrite) throws IOException;

    /**
     * Restore data from a file
     * @param pathToFile Full file name
     * @return True - data was restored successfully
     */
    boolean restoreFromFile(String pathToFile) throws IOException;
}
