package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.DuplicateEntityRecordException;
import dev.jcri.mdde.registry.store.exceptions.IllegalRegistryActionException;
import dev.jcri.mdde.registry.store.exceptions.UnknownEntityIdException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;

import java.util.Set;

public interface IWriteCommandHandler {
    /**
     * Insert tuple id to the specified node, optionally assigning it to a fragment
     * @param tupleId Tuple ID to be assigned to the specified node
     * @param nodeId Destination node
     * @return
     * @throws DuplicateEntityRecordException
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    boolean insertTuple(final String tupleId, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException;

    /**
     * Insert tuples to node as unassigned to a fragment
     * @param tupleIds Tuple IDs
     * @param nodeId Node ID
     * @throws DuplicateEntityRecordException
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    boolean insertTuple(final Set<String> tupleIds, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException;

    /**
     * Completely remove tuple from the registry with excluding from fragments
     * @param tupleId Tuple ID
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    boolean deleteTuple(final String tupleId) throws UnknownEntityIdException, WriteOperationException;


    /**
     * Form a fragment from tuples located on the same node
     * @param tupleIds Set of tuple IDs
     * @param fragmentId new Fragment ID
     * @return
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    boolean formFragment(final Set<String> tupleIds, final String fragmentId)
            throws UnknownEntityIdException, WriteOperationException, DuplicateEntityRecordException,
            IllegalRegistryActionException;


    /**
     * Append an unassigned tuple ID to existing fragment.
     * @param tupleId
     * @param fragmentId
     * @throws DuplicateEntityRecordException
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    boolean appendTupleToFragment(final String tupleId, final String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException;

    /**
     * Copy fragment tuples
     * @param fragmentId Fragment ID
     * @param sourceNodeId Where Fragment should be copied from
     * @param destinationNodeId Where it should be placed
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     * @throws IllegalRegistryActionException
     */
    boolean replicateFragment(final String fragmentId, final String sourceNodeId, final String destinationNodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException;

    /**
     * Remove a specific copy of a fragment
     * @param fragmentId ID of the fragment to be removed
     * @param nodeId ID of the node from which the fragment should be removed
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    boolean deleteFragmentExemplar(final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException;

    /**
     * Completely remove the fragment from the Registry, including all associated tuples
     * @param fragmentId Fragment ID to be removed
     * @return
     * @throws UnknownEntityIdException
     */
    String deleteFragmentCompletely(final String fragmentId) throws UnknownEntityIdException;

    /**
     * Initial node catalog population. Only allowed within an empty registry.
     * @param nodeIds Set of node IDs
     * @return
     * @throws IllegalRegistryActionException
     * @throws WriteOperationException
     */
    boolean populateNodes(final Set<String> nodeIds) throws IllegalRegistryActionException, WriteOperationException;

    /**
     * Add meta value to the fragment Globally, independent of nodes. Not the same as adding the same value to all fragment exemplars.
     * These functions should be kept separated.
     * @param fragmentId Fragment ID
     * @param metaField Name of the meta field (while specific length constraints depend on your backend implementation, better keep it short)
     * @param metaValue Vale of the meta serialized as string
     */
    boolean addMetaToFragmentGlobal(final String fragmentId, final String metaField, final String metaValue) throws UnknownEntityIdException, WriteOperationException;

    /**
     * Add meta value to the specific fragment exemplar
     * @param fragmentId Fragment ID
     * @param nodeId Node ID
     * @param metaField Name of the meta field (while specific length constraints depend on your backend implementation, better keep it short)
     * @param metaValue Vale of the meta serialized as string
     */
    boolean addMetaToFragmentExemplar(final String fragmentId, final String nodeId, final String metaField, final String metaValue) throws UnknownEntityIdException, WriteOperationException;

    /**
     * Remove all meta values for all fragments
     */
    boolean resetFragmentsMeta();

    /**
     * Clear all meta, fragments and tuples from the registry, leaving only empty nodes
     */
    boolean reset() throws WriteOperationException;
}
