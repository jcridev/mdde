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
public abstract class WriteCommandHandler<T> extends BaseCommandHandler {
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
    public final void runCommand(Commands writeCommand, List<Object> arguments)
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
//region Process query commands

    private String getPositionalArgumentError(final Commands command, final ExpectedCommandArgument argument, final int position){
        return String.format("%s must be invoked with %s at position %d", command.toString(), argument.toString(), position);
    }

    private void validateNotNullArguments(final List<Object> arguments, final Commands command){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments", command.toString()));
    }

    private void processInsertTupleCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        final var thisCommand = Commands.INSERT_TUPLE;
        validateNotNullArguments(arguments, thisCommand);

        final int tupleIdPosition = 0 ;
        var tupleId = (String) Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand, ARG_TUPLE_ID, tupleIdPosition));

        final int nodeIdPosition = 1;
        var nodeId = (String) Objects.requireNonNull(arguments.get(nodeIdPosition),
                getPositionalArgumentError(thisCommand, ARG_NODE_ID, nodeIdPosition));

        verifyAndRunInsertTuple(tupleId, nodeId);
    }

    private void processInsertTupleInBulkCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        final var thisCommand = Commands.INSERT_TUPLE_BULK;
        validateNotNullArguments(arguments, thisCommand);

        final int tupleIdPosition = 0;
        var tupleIdsArg = Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand, ARG_TUPLE_IDs, tupleIdPosition));
        if(!(tupleIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(getPositionalArgumentError(thisCommand, ARG_TUPLE_IDs, tupleIdPosition));
        }

        final int nodeIdPosition = 1;
        var nodeId = (String) Objects.requireNonNull(arguments.get(nodeIdPosition),
                getPositionalArgumentError(thisCommand, ARG_NODE_ID, nodeIdPosition));

        verifyAndRunInsertTuple((Set<String>) tupleIdsArg, nodeId);
    }

    private void processDeleteTupleCommand(final List<Object> arguments)
            throws ResponseSerializationException, UnknownEntityIdException, WriteOperationException {
        final var thisCommand = Commands.DELETE_TUPLE;
        validateNotNullArguments(arguments, thisCommand);

        final int tupleIdPosition = 0;
        var tupleId = (String) Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand, ARG_TUPLE_ID, tupleIdPosition));

        runCompleteTupleDeletion(tupleId); //TODO
    }

    private T processFormFragmentCommand(final List<Object> arguments)
            throws ResponseSerializationException, WriteOperationException, IllegalRegistryActionException, UnknownEntityIdException, DuplicateEntityRecordException {
        final var thisCommand = Commands.FORM_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand);

        final int tupleIdPosition = 0;
        var tupleIdsArg = Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand, ARG_TUPLE_IDs, tupleIdPosition));
        if(!(tupleIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a collection %s ",
                    thisCommand.toString(), ARG_TUPLE_IDs));
        }

        final int fragmentIdPosition = 1;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand, ARG_FRAGMENT_ID, fragmentIdPosition));

        final int nodeIdPosition = 2;
        var nodeId = (String) Objects.requireNonNull(arguments.get(nodeIdPosition),
                getPositionalArgumentError(thisCommand, ARG_NODE_ID, nodeIdPosition));

        return _serializer.serialize(verifyAndRunFormFragment((Set<String>) tupleIdsArg, fragmentId, nodeId));
    }

    private void processAppendToFragmentCommand(final List<Object> arguments)
            throws WriteOperationException {
        final var thisCommand = Commands.APPEND_TO_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand);

        final int tupleIdPosition = 0;
        var tupleId = (String) Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand, ARG_TUPLE_ID, tupleIdPosition));

        final int fragmentIdPosition = 1;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand, ARG_FRAGMENT_ID, fragmentIdPosition));

        runAppendTupleToFragment(tupleId, fragmentId);
    }

    private void processReplicateFragmentCommand(final List<Object> arguments)
            throws WriteOperationException {
        final var thisCommand = Commands.REPLICATE_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand);

        final int fragmentIdPosition = 0;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand, ARG_FRAGMENT_ID, fragmentIdPosition));

        final int nodeAPosition = 1;
        var nodeIdA = (String) Objects.requireNonNull(arguments.get(nodeAPosition),
                getPositionalArgumentError(thisCommand, ARG_NODE_ID, nodeAPosition));

        final int nodeBPosition = 2;
        var nodeIdB = (String) Objects.requireNonNull(arguments.get(nodeBPosition),
                getPositionalArgumentError(thisCommand, ARG_NODE_ID_B, nodeBPosition));

        runReplicateFragment(fragmentId, nodeIdA, nodeIdB);
    }

    private void processDeleteFragmentExemplar(final List<Object> arguments)
            throws WriteOperationException {
        final var thisCommand = Commands.DELETE_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand);

        final int fragmentIdPosition = 0;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand, ARG_FRAGMENT_ID, fragmentIdPosition));

        final int nodeAPosition = 1;
        var nodeIdA = (String) Objects.requireNonNull(arguments.get(nodeAPosition),
                getPositionalArgumentError(thisCommand, ARG_NODE_ID, nodeAPosition));

        runDeleteFragmentExemplar(fragmentId, nodeIdA);
    }

    private T processDestroyFragment(final List<Object> arguments)
            throws ResponseSerializationException {
        final var thisCommand = Commands.DESTROY_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand);

        final int fragmentIdPosition = 0;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand, ARG_FRAGMENT_ID, fragmentIdPosition));

        return _serializer.serialize(runCompleteFragmentDeletion(fragmentId));
    }

    private T processPopulateNodes(final List<Object> arguments)
            throws IllegalRegistryActionException, ResponseSerializationException, WriteOperationException {
        final var thisCommand = Commands.POPULATE_NODES;
        validateNotNullArguments(arguments, thisCommand);

        final int nodePosition = 0;
        var nodeIdsArg = Objects.requireNonNull(arguments.get(nodePosition),
                getPositionalArgumentError(thisCommand, ARG_NODE_IDs, nodePosition));

        if(!(nodeIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a set collection %s ",
                    thisCommand.toString(), ARG_NODE_IDs));
        }

        var currentNodes = readCommandHandler.runGetNodes();
        if(currentNodes != null && !currentNodes.isEmpty()){
            throw new IllegalRegistryActionException("Nodes population is now allowed in non-empty registry",
                    IllegalRegistryActionException.IllegalActions.AttemptToSeedNonEmptyRegistry);
        }

        return _serializer.serialize(runPopulateNodes((Set<String>)nodeIdsArg));
    }
//endregion
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
//endregion
//region Verify the correctness and validity of the invoked operation
    private void verifyAndRunInsertTuple(final String tupleId, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        if (readCommandHandler.runGetIsTupleExists(tupleId)) {
            throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
        }
        if (!readCommandHandler.runGetIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }

        runInsertTupleToNode(tupleId, nodeId);
    }

    private String verifyAndRunFormFragment(final Set<String> tupleIds, final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, DuplicateEntityRecordException, IllegalRegistryActionException {
        if(!readCommandHandler.runGetIsTuplesUnassigned(nodeId, tupleIds)){
            throw new IllegalRegistryActionException("Fragments can only be formed from colocated exiting tuples",
                    IllegalRegistryActionException.IllegalActions.FormingFragmentFromNonColocatedTuples);
        }
        if (readCommandHandler.runGetIsFragmentExists(fragmentId)) {
            throw new DuplicateEntityRecordException(RegistryEntityType.Fragment, fragmentId);
        }

        return runFormFragment(tupleIds, fragmentId, nodeId);
    }

    private void verifyAndRunInsertTuple(final Set<String> tupleIds, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {

        for(String tupleId: tupleIds){
            if (readCommandHandler.runGetIsTupleExists(tupleId)) {
                throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
            }
        }
        if (!readCommandHandler.runGetIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }

        runInsertTupleToNode(tupleIds, nodeId);
    }

    private void verifyAndRunAppendTupleToFragment(final String tupleId, final String fragmentId)
            throws UnknownEntityIdException, WriteOperationException {
        if (!readCommandHandler.runGetIsTupleExists(tupleId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Tuple, tupleId);
        }
        if (!readCommandHandler.runGetIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }

        runAppendTupleToFragment(tupleId, fragmentId);
    }

    private void verifyAndRunReplicateFragment(final String fragmentId, final String sourceNodeId, final String destinationNodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException {
        if (!readCommandHandler.runGetIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        if (!readCommandHandler.runGetIsNodeExists(sourceNodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, sourceNodeId);
        }
        if (!readCommandHandler.runGetIsNodeExists(destinationNodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, destinationNodeId);
        }
        if(sourceNodeId.equals(destinationNodeId)){
            throw new IllegalRegistryActionException("Source and destination nodes can't be the same",
                    IllegalRegistryActionException.IllegalActions.LocalFragmentReplication);
        }
        if(readCommandHandler.runGetIsNodeContainsFragment(destinationNodeId, fragmentId)){
            throw new IllegalRegistryActionException(String.format("Destination node %s already contains fragment %s",
                    destinationNodeId, fragmentId), IllegalRegistryActionException.IllegalActions.DuplicateFragmentReplication);
        }
        runReplicateFragment(fragmentId, sourceNodeId, destinationNodeId);
    }

    private void verifyAndRunDeleteFragmentExemplar(final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException {
        var fragmentCount = readCommandHandler.runGetCountFragment(fragmentId);
        if(fragmentCount == 0){
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        } else if (fragmentCount == 1){
            throw new IllegalRegistryActionException(String.format("Attempted to remove a unique fragment %s", fragmentId),
                    IllegalRegistryActionException.IllegalActions.UniqueFragmentRemoval);
        }
        if (!readCommandHandler.runGetIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }
        runDeleteFragmentExemplar(fragmentId, nodeId);
    }

    private String verifyAndRunCompleteFragmentDeletion(final String fragmentId)
            throws UnknownEntityIdException {
        if (!readCommandHandler.runGetIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        return runCompleteFragmentDeletion(fragmentId);
    }
//endregion
//region Abstract methods
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
//endregion
    /**
     * Registry manipulation commands available
     */
    public enum Commands {
        INSERT_TUPLE("INSERT", new ArrayList<>(){{add(ARG_TUPLE_ID); add(ARG_NODE_ID);}}),
        INSERT_TUPLE_BULK("INSERTMANY", new ArrayList<>(){{add(ARG_TUPLE_IDs); add(ARG_NODE_ID);}}),
        DELETE_TUPLE("DELTUPLE", new ArrayList<>(){{add(ARG_TUPLE_ID); }}),
        FORM_FRAGMENT("GROUP", new ArrayList<>(){{add(ARG_TUPLE_IDs); add(ARG_FRAGMENT_ID); add(ARG_NODE_ID);}}),
        APPEND_TO_FRAGMENT("APPEND", new ArrayList<>(){{add(ARG_TUPLE_ID); add(ARG_FRAGMENT_ID);}}),
        REPLICATE_FRAGMENT("REPLICATE", new ArrayList<>(){{add(ARG_FRAGMENT_ID); add(ARG_NODE_ID); add(ARG_NODE_ID_B);}}),
        DELETE_FRAGMENT("DELFRAGCOPY", new ArrayList<>(){{add(ARG_FRAGMENT_ID); add(ARG_NODE_ID);}}),
        DESTROY_FRAGMENT("ERASE", new ArrayList<>(){{add(ARG_FRAGMENT_ID);}}),
        POPULATE_NODES("ADDNODES", new ArrayList<>(){{add(ARG_NODE_IDs);}});

        private final String _command;
        private final List<ExpectedCommandArgument> _expectedArguments;
        private final int _numExpectedArguments;

        /**
         *
         * @param command
         * @param args List of expected arguments,in the order they should arrive
         */
        Commands(final String command, final List<ExpectedCommandArgument> args) {
            this._command = command;
            _expectedArguments = args;
            if(_expectedArguments == null){
                _numExpectedArguments = 0;
            }
            else {
                _numExpectedArguments = _expectedArguments.size();
            }
        }

        private static Map<String, Commands> _commandsMap = Arrays.stream(Commands.values())
                                                            .collect(Collectors.toMap(e -> e._command, e -> e));

        @Override
        public String toString() {
            return _command;
        }

        /**
         * Number of arguments that are expected by a specific command
         * @return
         */
        public int getNumberOfExpectedArguments(){
            return _numExpectedArguments;
        }

        /**
         * Get specific command by the text key tag
         * @param tag Tag
         * @return
         * @throws UnknownRegistryCommandExceptions
         */
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
}
