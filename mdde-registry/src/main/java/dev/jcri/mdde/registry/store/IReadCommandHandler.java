package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.response.FullRegistry;

import java.util.Set;

public interface IReadCommandHandler {
    /**
     * Get the full registry state
     * @return Full registry state
     * @throws ReadOperationException
     */
    FullRegistry getFullRegistry() throws ReadOperationException;

    /**
     * Get node IDs where the tuple is located
     * @param tupleId Tuple ID
     * @return
     */
    Set<String> getTupleNodes(final String tupleId);

    /**
     * Get fragment ID to which the tuple belongs, if unassigned, null is returned instead
     * @param tupleId Tuple ID
     * @return
     */
    String getTupleFragment(final String tupleId);

    /**
     * Get node IDs where the fragment is located
     * @param fragmentId Fragment ID
     * @return
     */
    Set<String> getFragmentNodes(final String fragmentId);

    /**
     * Get all fragment IDs located on the node
     * @param nodeId Node ID
     * @return
     */
    Set<String> getNodeFragments(final String nodeId);

    /**
     * Get tuple IDs assigned to the fragment
     * @param fragmentId Fragment ID
     * @return
     * @throws ReadOperationException
     */
    Set<String> getFragmentTuples(final String fragmentId) throws ReadOperationException;

    /**
     * Get the number of instances of the specific fragment, that are tracked by the registry
     * @param fragmentId Fragment ID
     * @return
     */
    int getCountFragment(final String fragmentId);

    /**
     * Get the number of instances of the specific tuple, that are tracked by the registry
     * @param tupleId Tuple ID
     * @return
     */
    int getCountTuple(final String tupleId);

    /**
     * Get a list of all node IDs that are part of this registry
     * @return
     */
    Set<String> getNodes();

    /**
     * Check if node exists in the registry
     * @param nodeId Node ID
     * @return True - node is found, False otherwise
     */
    boolean getIsNodeExists(final String nodeId);

    /**
     * Check if tuple exists in the registry
     * @param tupleId Tuple ID
     * @return
     */
    boolean getIsTupleExists(final String tupleId);

    /**
     * Check if fragment exist
     * @param fragmentId Fragment ID
     * @return
     */
    boolean getIsFragmentExists(final String fragmentId);

    /**
     * Get tuples that were not yet assigned to any fragment on a specific node
     * @param nodeId Node ID
     * @return
     */
    public Set<String> getUnassignedTuples(String nodeId);

    /**
     * Check if a specific tuple is in the unassigned heap on a specific node
     * @param nodeId Node ID
     * @param tupleIds Tuple ID
     * @return
     */
    Boolean getIsTuplesUnassigned(String nodeId, Set<String> tupleIds);

    /**
     * Check of a specific node is stored on a specific node
     * @param nodeId Node ID
     * @param fragmentId Fragment ID
     * @return
     */
    Boolean getIsNodeContainsFragment(String nodeId, String fragmentId);

    /**
     * Get all fragment IDs registered within the registry
     * @return
     */
    Set<String> getAllFragmentIds();
}
