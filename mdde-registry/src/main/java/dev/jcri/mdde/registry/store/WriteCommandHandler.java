package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.*;
import dev.jcri.mdde.registry.store.response.serialization.IResponseSerializer;
import org.jetbrains.annotations.NotNull;

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
    final ReadCommandHandler<T> _readCommandHandler;
    /**
     * Serialization handler for the commands execution
     */
    IResponseSerializer<T> _serializer;

    public WriteCommandHandler(ReadCommandHandler<T> readCommandHandler, IResponseSerializer<T> serializer){
        Objects.requireNonNull(readCommandHandler, "Handler for reads can't be null");
        Objects.requireNonNull(serializer, "Response serialization handler is not supplied ");
        _serializer = serializer;
        _readCommandHandler = readCommandHandler;
    }

    /**
     * Execute the specified query-like command
     * @param writeCommand WriteCommandHandler.Commands
     * @param arguments Key-value pairs
     * @return JSON serialized result
     */
    public final T runCommand(Commands writeCommand, Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException,
                    ResponseSerializationException, IllegalRegistryActionException, WriteOperationException {
        _commandExecutionLock.lock(); // Ensure sequential execution of registry manipulation
        try {
            switch (writeCommand) {
                case INSERT_TUPLE:
                    return processInsertTupleCommand(arguments);
                case INSERT_TUPLE_BULK:
                    return processInsertTupleInBulkCommand(arguments);
                case DELETE_TUPLE:
                    return processDeleteTupleCommand(arguments);
                case FORM_FRAGMENT:
                    return processFormFragmentCommand(arguments);
                case APPEND_TO_FRAGMENT:
                    return processAppendToFragmentCommand(arguments);
                case REPLICATE_FRAGMENT:
                    return processReplicateFragmentCommand(arguments);
                case DELETE_FRAGMENT:
                    return processDeleteFragmentExemplar(arguments);
                case DESTROY_FRAGMENT:
                    return processDestroyFragment(arguments);
                case POPULATE_NODES:
                    return processPopulateNodes(arguments);
            }
        }
        finally {
            _commandExecutionLock.unlock();
        }

        return null;
    }

    private T processInsertTupleCommand(final Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.INSERT_TUPLE.toString()));
        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_TUPLE_ID));

        var nodeId = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        String fragmentId = (String) arguments.get(ARG_FRAGMENT_ID);

        return _serializer.serialize(insertTuple(tupleId, nodeId, fragmentId));
    }

    private T processInsertTupleInBulkCommand(final Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.INSERT_TUPLE.toString()));
        var tupleIdsArg = Objects.requireNonNull(arguments.get(ARG_TUPLE_IDs), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_TUPLE_IDs));

        if(!(tupleIdsArg instanceof List<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a collection %s ",
                    Commands.INSERT_TUPLE.toString(), ARG_TUPLE_IDs));
        }

        var nodeId = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        String fragmentId = (String) arguments.get(ARG_FRAGMENT_ID);

        return _serializer.serialize(insertTuple((List<String>) tupleIdsArg, nodeId, fragmentId));
    }

    private T processDeleteTupleCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.DELETE_TUPLE.toString()));
        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.DELETE_TUPLE.toString(), ARG_TUPLE_ID));

        return _serializer.serialize(runDeleteTuple(tupleId));
    }

    private T processFormFragmentCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var tupleIdsArg = Objects.requireNonNull(arguments.get(ARG_TUPLE_IDs), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_TUPLE_IDs));

        if(!(tupleIdsArg instanceof List<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a collection %s ",
                    Commands.FORM_FRAGMENT.toString(), ARG_TUPLE_IDs));
        }

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return _serializer.serialize(runFormFragment((List<String>) tupleIdsArg, fragmentId));
    }

    private T processAppendToFragmentCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.DELETE_TUPLE.toString(), ARG_TUPLE_ID));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return _serializer.serialize(runAppendTupleToFragment(tupleId, fragmentId));
    }

    private T processReplicateFragmentCommand(final Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        var nodeIdA = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));
        var nodeIdB = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID_B), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID_B));

        return _serializer.serialize(runReplicateFragment(fragmentId, nodeIdA, nodeIdB));
    }

    private T processDeleteFragmentExemplar(final Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        var nodeIdA = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        return _serializer.serialize(runDeleteFragmentExemplar(fragmentId, nodeIdA));
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

        var currentNodes = _readCommandHandler.getNodes();
        if(currentNodes != null && !currentNodes.isEmpty()){
            throw new IllegalRegistryActionException("Nodes population is now allowed in non-empty registry");
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
     * @param fragmentId
     * @return
     * @throws DuplicateEntityRecordException
     * @throws UnknownEntityIdException
     */
    public final String insertTuple(final String tupleId, final String nodeId, String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            return verifyAndRunInsertTuple(tupleId, nodeId, fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    private final String verifyAndRunInsertTuple(final String tupleId, final String nodeId, String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException {
        if (_readCommandHandler.getIsTupleExists(tupleId)) {
            throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
        }
        if (!_readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        if (!_readCommandHandler.getIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }

        return runInsertTuple(tupleId, nodeId, fragmentId);
    }

    public final String insertTuple(final List<String> tupleIds, final String nodeId, String fragmentId){
        _commandExecutionLock.lock();
        try {
            throw new UnsupportedOperationException();
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    private final String verifyAndRunInsertTuple(final List<String> tupleIds, final String nodeId, String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException {

        for(String tupleId: tupleIds){
            if (_readCommandHandler.getIsTupleExists(tupleId)) {
                throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
            }
        }

        if (!_readCommandHandler.getIsFragmentExists(fragmentId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
        }
        if (!_readCommandHandler.getIsNodeExists(nodeId)) {
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }

        return runInsertTuple(tupleIds, nodeId, fragmentId);
    }

    public final String deleteTuple(final String tupleId){
        _commandExecutionLock.lock();
        try {
            return runDeleteTuple(tupleId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final String formFragment(final List<String> tupleIds, final String fragmentId)
            throws UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            for(String tupleId: tupleIds){
                if (_readCommandHandler.getIsTupleExists(tupleId)) {
                    throw new UnknownEntityIdException(RegistryEntityType.Tuple, tupleId);
                }
            }

            if (!_readCommandHandler.getIsFragmentExists(fragmentId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
            }

            return runFormFragment(tupleIds, fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final String appendTupleToFragment(final String tupleId, final String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            if (_readCommandHandler.getIsTupleExists(tupleId)) {
                throw new DuplicateEntityRecordException(RegistryEntityType.Tuple, tupleId);
            }
            if (!_readCommandHandler.getIsFragmentExists(fragmentId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
            }

            return runAppendTupleToFragment(tupleId, fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final String replicateFragment( final String fragmentId,
                                    final String sourceNodeId,
                                    final String destinationNodeId)
            throws UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            if (!_readCommandHandler.getIsFragmentExists(fragmentId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
            }
            if (!_readCommandHandler.getIsNodeExists(sourceNodeId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Node, sourceNodeId);
            }
            if (!_readCommandHandler.getIsNodeExists(destinationNodeId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Node, destinationNodeId);
            }

            return runReplicateFragment(fragmentId, sourceNodeId, destinationNodeId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final String deleteFragmentExemplar(final String fragmentId, final String nodeId)
            throws UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            if (!_readCommandHandler.getIsFragmentExists(fragmentId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
            }
            if (!_readCommandHandler.getIsNodeExists(nodeId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
            }
            return runDeleteFragmentExemplar(fragmentId, nodeId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final String deleteFragmentCompletely(final String fragmentId)
            throws UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            if (!_readCommandHandler.getIsFragmentExists(fragmentId)) {
                throw new UnknownEntityIdException(RegistryEntityType.Fragment, fragmentId);
            }
            return runCompleteFragmentDeletion(fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    /**
     * Insert tuple to the registry, optionally attaching it to a fragment right away
     * @param tupleId
     * @param fragmentId
     * @param nodeId Destination node
     * @return
     */
    protected abstract String runInsertTuple(final String tupleId, final String nodeId, String fragmentId);

    /**
     * Bulk insertion (intended to initial data population mostly)
     * @param tupleId
     * @param nodeId
     * @param fragmentId
     * @return
     */
    protected abstract String runInsertTuple(final List<String> tupleId, final String nodeId, String fragmentId);

    /**
     * Deleter tuple and exclude from all of the fragments it's in
     * @param tupleId
     * @return
     */
    protected abstract String runDeleteTuple(final String tupleId);

    /**
     * Create fragment including the specified tuples. A tuple can belong to only one fragment at a time
     * @param tupleId
     * @return
     */
    protected abstract String runFormFragment(final List<String> tupleId, String fragmentId);

    /**
     * Attach tuple to a fragment. If a fragment is already part of a tuple, exclude it from the current fragment.
     * @param tupleId Tuple Id that should be added to the specified fragment
     * @param fragmentId Destination fragment Id
     * @return
     */
    protected abstract String runAppendTupleToFragment(final String tupleId, final String fragmentId);

    /**
     * Create a copy of a fragment
     * @param fragmentId
     * @param sourceNodeId
     * @param destinationNodeId
     * @return
     */
    protected abstract String runReplicateFragment(final String fragmentId,
                                                   final String sourceNodeId,
                                                   final String destinationNodeId);

    /**
     * Remove replicated copy of a fragment, if a fragment it not
     * @param fragmentId Fragment Id
     * @param nodeId Node from which the fragment must be erased
     * @return
     */
    protected abstract String runDeleteFragmentExemplar(final String fragmentId, final String nodeId);

    /**
     * **Destructive operation**, removes the fragment completely from the registry.
     * @param fragmentId
     * @return
     */
    protected abstract String runCompleteFragmentDeletion(final String fragmentId);

    /**
     * Add nodes to the store
     * @param nodeIds
     * @return
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
