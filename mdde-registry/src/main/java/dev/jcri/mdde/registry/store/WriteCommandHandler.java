package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.*;
import dev.jcri.mdde.registry.store.response.serialization.IResponseSerializer;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Abstract bas class facilitating interaction with the underlying backend.
 * Available actions can be called through a query-like interface as well as directly.
 *
 * Subclass with implementing abstract functions with concrete underlying registry storage.
 */
public abstract class WriteCommandHandler<T> {
    /**
     * Lock ensuring sequential execution of the commands
     */
    final ReentrantLock _commandExecutionLock = new ReentrantLock();
    /**
     * Corresponding reader for the registry store
     */
    protected final ReadCommandHandler<T> readCommandHandler;
    /**
     * Serialization handler for the commands execution
     */
    IResponseSerializer<T> _serializer;

    public WriteCommandHandler(ReadCommandHandler<T> readCommandHandler, IResponseSerializer<T> serializer){
        Objects.requireNonNull(readCommandHandler, "Handler for reads can't be null");
        Objects.requireNonNull(serializer, "Response serialization handler is not supplied ");
        _serializer = serializer;
        this.readCommandHandler = readCommandHandler;
    }

    /**
     * Execute the specified query-like command
     * @param writeCommand WriteCommandHandler.Commands
     * @param arguments Key-value pairs
     */
    public final void runCommand(Commands writeCommand, Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException,
                    ResponseSerializationException, IllegalRegistryActionException, WriteOperationException {
        _commandExecutionLock.lock(); // Ensure sequential execution of registry manipulation
        try {
            switch (writeCommand) {
                case INSERT_TUPLE:
                    processInsertTupleCommand(arguments);
                case INSERT_TUPLE_BULK:
                    processInsertTupleInBulkCommand(arguments);
                case DELETE_TUPLE:
                    processDeleteTupleCommand(arguments);
                case FORM_FRAGMENT:
                    processFormFragmentCommand(arguments);
                case APPEND_TO_FRAGMENT:
                    processAppendToFragmentCommand(arguments);
                case REPLICATE_FRAGMENT:
                    processReplicateFragmentCommand(arguments);
                case DELETE_FRAGMENT:
                    processDeleteFragmentExemplar(arguments);
                case DESTROY_FRAGMENT:
                    processDestroyFragment(arguments);
                case POPULATE_NODES:
                    processPopulateNodes(arguments);
            }
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    private void processInsertTupleCommand(final Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.INSERT_TUPLE.toString()));
        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID),
                String.format("%s must be invoked with %s", Commands.INSERT_TUPLE.toString(), ARG_TUPLE_ID));
        var nodeId = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID),
                String.format("%s must be invoked with %s", Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        verifyAndRunInsertTuple(tupleId, nodeId);
    }

    private void processInsertTupleInBulkCommand(final Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.INSERT_TUPLE.toString()));
        var tupleIdsArg = Objects.requireNonNull(arguments.get(ARG_TUPLE_IDs), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_TUPLE_IDs));

        if(!(tupleIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a collection %s ",
                    Commands.INSERT_TUPLE.toString(), ARG_TUPLE_IDs));
        }
        var nodeId = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        verifyAndRunInsertTuple((Set<String>) tupleIdsArg, nodeId);
    }

    private void processDeleteTupleCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException, UnknownEntityIdException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.DELETE_TUPLE.toString()));
        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.DELETE_TUPLE.toString(), ARG_TUPLE_ID));

        runCompleteTupleDeletion(tupleId); //TODO
    }

    private T processFormFragmentCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var tupleIdsArg = Objects.requireNonNull(arguments.get(ARG_TUPLE_IDs), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_TUPLE_IDs));

        if(!(tupleIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a collection %s ",
                    Commands.FORM_FRAGMENT.toString(), ARG_TUPLE_IDs));
        }

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return _serializer.serialize(runFormFragment((Set<String>) tupleIdsArg, fragmentId));
    }

    private void processAppendToFragmentCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.DELETE_TUPLE.toString(), ARG_TUPLE_ID));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        runAppendTupleToFragment(tupleId, fragmentId);
    }

    private void processReplicateFragmentCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        var nodeIdA = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));
        var nodeIdB = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID_B), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID_B));

        runReplicateFragment(fragmentId, nodeIdA, nodeIdB);
    }

    private void processDeleteFragmentExemplar(final Map<String, Object> arguments)
            throws ResponseSerializationException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        var nodeIdA = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        runDeleteFragmentExemplar(fragmentId, nodeIdA);
    }

    private T processDestroyFragment(final Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return _serializer.serialize(runCompleteFragmentDeletion(fragmentId));
    }

    private T processPopulateNodes(final Map<String, Object> arguments)
            throws IllegalRegistryActionException, ResponseSerializationException, WriteOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.POPULATE_NODES.toString()));

        var currentNodes = readCommandHandler.getNodes();
        if(currentNodes != null && !currentNodes.isEmpty()){
            throw new IllegalRegistryActionException("Nodes population is now allowed in non-empty registry", IllegalRegistryActionException.IllegalActions.AttemptToSeedNonEmptyRegistry);
        }

        var nodeIdsArg = Objects.requireNonNull(arguments.get(ARG_NODE_IDs), String.format("%s must be invoked with %s",
                Commands.POPULATE_NODES.toString(), ARG_NODE_IDs));

        if(!(nodeIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a set collection %s ",
                    Commands.POPULATE_NODES.toString(), ARG_NODE_IDs));
        }

        return _serializer.serialize(runPopulateNodes((Set<String>)nodeIdsArg));
    }

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
    public final String formFragment(final Set<String> tupleIds, final String fragmentId)
            throws UnknownEntityIdException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            return verifyAndRunFormFragment(tupleIds, fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    private String verifyAndRunFormFragment(final Set<String> tupleIds, final String fragmentId)
            throws UnknownEntityIdException, WriteOperationException{
        for(String tupleId: tupleIds){
            if (readCommandHandler.getIsTupleExists(tupleId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Tuple, tupleId);
            }
        }

        if (!readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }

        return runFormFragment(tupleIds, fragmentId);
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

    private void verifyAndRunAppendTupleToFragment(final String tupleId, final String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        if (readCommandHandler.getIsTupleExists(tupleId)) {
            throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
        }
        if (!readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }

        runAppendTupleToFragment(tupleId, fragmentId);
    }

    public final void replicateFragment(final String fragmentId, final String sourceNodeId, final String destinationNodeId)
            throws UnknownEntityIdException, WriteOperationException {
        _commandExecutionLock.lock();
        try {
            verifyAndRunReplicateFragment(fragmentId, sourceNodeId, destinationNodeId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    private void verifyAndRunReplicateFragment(final String fragmentId, final String sourceNodeId, final String destinationNodeId)
            throws UnknownEntityIdException, WriteOperationException {
        if (!readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        if (!readCommandHandler.getIsNodeExists(sourceNodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, sourceNodeId);
        }
        if (!readCommandHandler.getIsNodeExists(destinationNodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, destinationNodeId);
        }

        runReplicateFragment(fragmentId, sourceNodeId, destinationNodeId);
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

    private String verifyAndRunCompleteFragmentDeletion(final String fragmentId)
            throws UnknownEntityIdException {
        if (!readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        return runCompleteFragmentDeletion(fragmentId);
    }

    /**
     * Populate node catalog
     * @param nodeIds Node IDs
     * @throws WriteOperationException
     */
    public final void populateNodes(Set<String> nodeIds) throws WriteOperationException {
        Objects.requireNonNull(nodeIds, "nodeIds can't be null");
        runPopulateNodes(nodeIds);
    }

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
    protected abstract String runFormFragment(final Set<String> tupleIds, String fragmentId) throws WriteOperationException;

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
    /**
     * Registry manipulation commands available
     */
    public enum Commands {
        INSERT_TUPLE("INSERT"),
        INSERT_TUPLE_BULK("INSERTMANY"),
        DELETE_TUPLE("DELTUPLE"),
        FORM_FRAGMENT("GROUP"),
        APPEND_TO_FRAGMENT("APPEND"),
        REPLICATE_FRAGMENT("REPLICATE"),
        DELETE_FRAGMENT("DELFRAGCOPY"),
        DESTROY_FRAGMENT("ERASE"),
        POPULATE_NODES("ADDNODES");

        private final String _command;
        /**
         * @param command
         */
        Commands(final String command) {
            this._command = command;
        }

        private static Map<String, Commands> _commandsMap = Arrays.stream(Commands.values())
                                                            .collect(Collectors.toMap(e -> e._command, e -> e));

        @Override
        public String toString() {
            return _command;
        }

        public static Commands getCommandTag(String tag) throws UnknownRegistryCommandExceptions {
            if(tag == null || tag.isEmpty()){
                throw new IllegalArgumentException("tag can't be null or empty");
            }
            var command = _commandsMap.get(tag);
            if(command == null){
                throw new UnknownRegistryCommandExceptions(tag);
            }
            return command;
        }
    }

    public final String ARG_TUPLE_ID = "tid";
    public final String ARG_TUPLE_IDs = "tids";
    public final String ARG_NODE_ID = "nid";
    public final String ARG_NODE_IDs = "nids";
    public final String ARG_NODE_ID_B = "nidb";
    public final String ARG_FRAGMENT_ID = "fid";
}
