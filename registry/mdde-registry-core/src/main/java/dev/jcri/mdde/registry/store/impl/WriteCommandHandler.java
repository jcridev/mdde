package dev.jcri.mdde.registry.store.impl;

import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.*;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class facilitating interaction with the underlying backend.
 * Available actions can be called through a query-like interface as well as directly.
 *
 * Subclass with implementing abstract functions with concrete underlying registry storage.
 */
public abstract class WriteCommandHandler implements IWriteCommandHandler {
    /**
     * Lock ensuring sequential execution of the commands
     */
    final ReentrantLock _commandExecutionLock = new ReentrantLock();

    /**
     * Corresponding reader for the registry store
     */
    protected final IReadCommandHandler readCommandHandler;

    public WriteCommandHandler(IReadCommandHandler readCommandHandler){
        Objects.requireNonNull(readCommandHandler, "Handler for reads can't be null");
        this.readCommandHandler = readCommandHandler;
    }

//region Public direct programmatic commands
    /**
     * Insert tuple id to the specified node, optionally assigning it to a fragment
     * @param tupleId
     * @param nodeId
     * @return
     * @throws DuplicateEntityRecordException
     * @throws UnknownEntityIdException
     */
    public final void insertTuple(final String tupleId, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            verifyAndRunInsertTuple(tupleId, nodeId);
        }
        finally {
           _commandExecutionLock.unlock();
        }
    }

    /**
     * Insert tuples to node as unassigned to a fragment
     * @param tupleIds Tuple IDs
     * @param nodeId Node ID
     * @throws DuplicateEntityRecordException
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    public final void insertTuple(final Set<String> tupleIds, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException{
        _commandExecutionLock.lock();
        try {
            verifyAndRunInsertTuple(tupleIds, nodeId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Completely remove tuple from the registry with excluding from fragments
     * @param tupleId Tuple ID
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    public final void deleteTuple(final String tupleId) throws UnknownEntityIdException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            runCompleteTupleDeletion(tupleId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Form a fragment from tuples located on the same node
     * @param tupleIds Set of tuple IDs
     * @param fragmentId new Fragment ID
     * @return
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    public final String formFragment(final Set<String> tupleIds, final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, DuplicateEntityRecordException, IllegalRegistryActionException {
        _commandExecutionLock.lock();
        try {
            return verifyAndRunFormFragment(tupleIds, fragmentId, nodeId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final void appendTupleToFragment(final String tupleId, final String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            verifyAndRunAppendTupleToFragment(tupleId, fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final void replicateFragment(final String fragmentId, final String sourceNodeId, final String destinationNodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException {
        _commandExecutionLock.lock();
        try {
            verifyAndRunReplicateFragment(fragmentId, sourceNodeId, destinationNodeId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Remove a specific copy of a fragment
     * @param fragmentId ID of the fragment to be removed
     * @param nodeId ID of the node from which the fragment should be removed
     * @throws UnknownEntityIdException
     * @throws WriteOperationException
     */
    public final void deleteFragmentExemplar(final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException {
        _commandExecutionLock.lock();
        try {
            verifyAndRunDeleteFragmentExemplar(fragmentId, nodeId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Completely remove the fragment from the Registry, including all associated tuples
     * @param fragmentId Fragment ID to be removed
     * @return
     * @throws UnknownEntityIdException
     */
    public final String deleteFragmentCompletely(final String fragmentId)
            throws UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            return verifyAndRunCompleteFragmentDeletion(fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final boolean populateNodes(final Set<String> nodeIds)
            throws IllegalRegistryActionException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            var currentNodes = readCommandHandler.getNodes();
            if(currentNodes != null && !currentNodes.isEmpty()){
                throw new IllegalRegistryActionException("Nodes population is now allowed in non-empty registry",
                        IllegalRegistryActionException.IllegalActions.AttemptToSeedNonEmptyRegistry);
            }

            return runPopulateNodes((Set<String>)nodeIds);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final void addMetaToFragmentGlobal(final String fragmentId, final String metaField, final String metaValue)
            throws UnknownEntityIdException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            if(fragmentId == null || fragmentId.isEmpty()){
                throw new IllegalArgumentException("Fragment ID can't be null or empty");
            }
            if(metaField == null || metaField.isEmpty()){
                throw new IllegalArgumentException("Meta field name can't be null or empty");
            }

            if(!readCommandHandler.getIsFragmentExists(fragmentId)){
                throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
            }
            runAddMetaToFragmentGlobal(fragmentId, metaField, metaValue);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final void addMetaToFragmentExemplar(final String fragmentId, final String nodeId, final String metaField, final String metaValue)
            throws UnknownEntityIdException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            if(fragmentId == null || fragmentId.isEmpty()){
                throw new IllegalArgumentException("Fragment ID can't be null or empty");
            }
            if(metaField == null || metaField.isEmpty()){
                throw new IllegalArgumentException("Meta field name can't be null or empty");
            }
            if(nodeId == null || nodeId.isEmpty()){
                throw new IllegalArgumentException("Meta field name can't be null or empty");
            }

            if(!readCommandHandler.getIsNodeContainsFragment(nodeId, fragmentId)){
                throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
            }
            runAddMetaToFragmentExemplar(fragmentId, nodeId, metaField, metaValue);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Remove all meta values for all fragments
     */
    @Override
    public final void resetFragmentsMeta(){
        _commandExecutionLock.lock();
        try {
            runResetFragmentsMeta();
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Clear the registry
     */
    @Override
    public final void reset() throws WriteOperationException {
        _commandExecutionLock.lock();
        try {
            runFlush();
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

//endregion
//region Verify the correctness and validity of the invoked operation
    private void verifyAndRunInsertTuple(final String tupleId, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        if (readCommandHandler.getIsTupleExists(tupleId)) {
            throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
        }
        if (!readCommandHandler.getIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }

        runInsertTupleToNode(tupleId, nodeId);
    }

    private String verifyAndRunFormFragment(final Set<String> tupleIds, final String fragmentId, final String nodeId)
            throws WriteOperationException, DuplicateEntityRecordException, IllegalRegistryActionException {
        if(!readCommandHandler.getIsTuplesUnassigned(nodeId, tupleIds)){
            throw new IllegalRegistryActionException("Fragments can only be formed from colocated exiting tuples",
                    IllegalRegistryActionException.IllegalActions.FormingFragmentFromNonColocatedTuples);
        }
        if (readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new DuplicateEntityRecordException(RegistryEntityType.Fragment, fragmentId);
        }

        return runFormFragment(tupleIds, fragmentId, nodeId);
    }

    private void verifyAndRunInsertTuple(final Set<String> tupleIds, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {

        for(String tupleId: tupleIds){
            if (readCommandHandler.getIsTupleExists(tupleId)) {
                throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
            }
        }
        if (!readCommandHandler.getIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }

        runInsertTupleToNode(tupleIds, nodeId);
    }

    private void verifyAndRunAppendTupleToFragment(final String tupleId, final String fragmentId)
            throws UnknownEntityIdException, WriteOperationException {
        if (!readCommandHandler.getIsTupleExists(tupleId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Tuple, tupleId);
        }
        if (!readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }

        runAppendTupleToFragment(tupleId, fragmentId);
    }

    private void verifyAndRunReplicateFragment(final String fragmentId, final String sourceNodeId, final String destinationNodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException {
        if (!readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        if (!readCommandHandler.getIsNodeExists(sourceNodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, sourceNodeId);
        }
        if (!readCommandHandler.getIsNodeExists(destinationNodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, destinationNodeId);
        }
        if(sourceNodeId.equals(destinationNodeId)){
            throw new IllegalRegistryActionException("Source and destination nodes can't be the same",
                    IllegalRegistryActionException.IllegalActions.LocalFragmentReplication);
        }
        if(readCommandHandler.getIsNodeContainsFragment(destinationNodeId, fragmentId)){
            throw new IllegalRegistryActionException(String.format("Destination node %s already contains fragment %s",
                    destinationNodeId, fragmentId), IllegalRegistryActionException.IllegalActions.DuplicateFragmentReplication);
        }
        runReplicateFragment(fragmentId, sourceNodeId, destinationNodeId);
    }

    private void verifyAndRunDeleteFragmentExemplar(final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException {
        var fragmentCount = readCommandHandler.getCountFragment(fragmentId);
        if(fragmentCount == 0){
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        } else if (fragmentCount == 1){
            throw new IllegalRegistryActionException(String.format("Attempted to remove a unique fragment %s", fragmentId),
                    IllegalRegistryActionException.IllegalActions.UniqueFragmentRemoval);
        }
        if (!readCommandHandler.getIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }
        runDeleteFragmentExemplar(fragmentId, nodeId);
    }

    private String verifyAndRunCompleteFragmentDeletion(final String fragmentId)
            throws UnknownEntityIdException {
        if (!readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        return runCompleteFragmentDeletion(fragmentId);
    }
//endregion
//region Abstract methods
    /**
     * Insert tuple to the registry, optionally attaching it to a fragment right away
     * @param tupleId Tuple ID
     * @param nodeId Destination node
     */
    protected abstract void runInsertTupleToNode(final String tupleId, final String nodeId) throws UnknownEntityIdException, WriteOperationException;

    /**
     * Bulk insert tuple to the registry, optionally attaching it to a fragment right away
     * @param tupleIds Tuple IDs
     * @param nodeId Destination node
     */
    protected abstract void runInsertTupleToNode(final Set<String> tupleIds, String nodeId) throws WriteOperationException, UnknownEntityIdException;

    /**
     * Insert tuple to a specific fragment
     * @param tupleId Tuple ID
     * @param fragmentId Destination fragment ID
     */
    protected abstract void runInsertTupleToFragment(final String tupleId, String fragmentId) throws WriteOperationException;
    /**
     * Bulk insertion to a specific fragment
     * @param tupleIds
     * @param fragmentId
     */
    protected abstract void runInsertTupleToFragment(final Set<String> tupleIds, String fragmentId) throws WriteOperationException;

    /**
     * Deleter tuple and exclude from all of the fragments it's in
     * @param tupleId
     * @return
     */
    protected abstract void runCompleteTupleDeletion(final String tupleId) throws UnknownEntityIdException, WriteOperationException;

    /**
     * Create fragment including the specified tuples. A tuple can belong to only one fragment at a time
     * @param tupleIds IDs of Tuples that will be placed into the same fragment
     * @return
     */
    protected abstract String runFormFragment(final Set<String> tupleIds, String fragmentId, String nodeId) throws WriteOperationException;

    /**
     * Attach tuple to a fragment. If a fragment is already part of a tuple, exclude it from the current fragment.
     * @param tupleId Tuple Id that should be added to the specified fragment
     * @param fragmentId Destination fragment Id
     */
    protected abstract void runAppendTupleToFragment(final String tupleId, final String fragmentId) throws WriteOperationException;

    /**
     * Create a copy of a fragment
     * @param fragmentId Fragment ID
     * @param sourceNodeId ID of the source node from where the fragment is copied
     * @param destinationNodeId ID of the destination node where the copy is placed
     */
    protected abstract void runReplicateFragment(final String fragmentId,
                                                   final String sourceNodeId,
                                                   final String destinationNodeId) throws WriteOperationException;

    /**
     * Remove replicated copy of a fragment, if a fragment it not
     * @param fragmentId Fragment Id
     * @param nodeId Node from which the fragment must be erased
     */
    protected abstract void runDeleteFragmentExemplar(final String fragmentId, final String nodeId) throws WriteOperationException;

    /**
     * **Destructive operation**, removes the fragment completely from the registry.
     * @param fragmentId Fragment ID
     * @return
     */
    protected abstract String runCompleteFragmentDeletion(final String fragmentId);

    /**
     * Add nodes to the store
     * @param nodeIds Set of data storage node IDs that are tracked by this registry
     * @return True - all of the nodes were added
     */
    protected abstract boolean runPopulateNodes(final Set<String> nodeIds) throws WriteOperationException;

    protected abstract void runAddMetaToFragmentGlobal(final String fragmentId,
                                                       final String metaField,
                                                       final String metaValue)
            throws WriteOperationException;

    protected abstract void runAddMetaToFragmentExemplar(final String fragmentId,
                                                         final String nodeId,
                                                         final String metaField,
                                                         final String metaValue)
            throws WriteOperationException;


    protected abstract void runResetFragmentsMeta();

    protected abstract void runFlush();
//endregion

}
