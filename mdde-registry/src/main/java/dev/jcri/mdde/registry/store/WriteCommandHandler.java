package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.DuplicateEntityRecordException;
import dev.jcri.mdde.registry.store.exceptions.RegistryEntityType;
import dev.jcri.mdde.registry.store.exceptions.UnknownEntityIdException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Abstract bas class facilitating interaction with the underlying backend.
 * Available actions can be called through a query-like interface as well as directly.
 *
 * Subclass with implementing abstract functions with concrete underlying registry storage.
 */
public abstract class WriteCommandHandler {
    /**
     * Lock ensuring sequential execution of the commands
     */
    final ReentrantLock _commandExecutionLock = new ReentrantLock();
    /**
     * Corresponding reader for the registry store
     */
    final ReadCommandHandler _readCommandHandler;

    public WriteCommandHandler(ReadCommandHandler readCommandHandler){
        Objects.requireNonNull(readCommandHandler, "Handler for reads can't be null");
        _readCommandHandler = readCommandHandler;
    }

    /**
     * Execute the specified query-like command
     * @param writeCommand WriteCommandHandler.Commands
     * @param arguments Key-value pairs
     * @return JSON serialized result
     */
    public final String runCommand(Commands writeCommand, Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException {
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
            }
        }
        finally {
            _commandExecutionLock.unlock();
        }

        return null;
    }

    private String processInsertTupleCommand(final Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.INSERT_TUPLE.toString()));
        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_TUPLE_ID));

        var nodeId = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        String fragmentId = (String) arguments.get(ARG_FRAGMENT_ID);

        return insertTuple(tupleId, nodeId, fragmentId);
    }

    private String processInsertTupleInBulkCommand(final Map<String, Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException {
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

        return insertTuple((List<String>) tupleIdsArg, nodeId, fragmentId);
    }

    private String processDeleteTupleCommand(final Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.DELETE_TUPLE.toString()));
        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.DELETE_TUPLE.toString(), ARG_TUPLE_ID));

        return runDeleteTuple(tupleId);
    }

    private String processFormFragmentCommand(final Map<String, Object> arguments){
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

        return runFormFragment((List<String>) tupleIdsArg, fragmentId);
    }

    private String processAppendToFragmentCommand(final Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.DELETE_TUPLE.toString(), ARG_TUPLE_ID));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return runAppendTupleToFragment(tupleId, fragmentId);
    }

    private String processReplicateFragmentCommand(final Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        var nodeIdA = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));
        var nodeIdB = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID_B), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID_B));

        return runReplicateFragment(fragmentId, nodeIdA, nodeIdB);
    }

    private String processDeleteFragmentExemplar(final Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        var nodeIdA = (String) Objects.requireNonNull(arguments.get(ARG_NODE_ID), String.format("%s must be invoked with %s",
                Commands.INSERT_TUPLE.toString(), ARG_NODE_ID));

        return runDeleteFragmentExemplar(fragmentId, nodeIdA);
    }

    private String processDestroyFragment(final Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return runCompleteFragmentDeletion(fragmentId);
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
    public final String insertTuple(@NotNull final String tupleId, @NotNull final String nodeId, String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException {
        _commandExecutionLock.lock();
        try {
            return verifyAndRunInsertTuple(tupleId, nodeId, fragmentId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    private final String verifyAndRunInsertTuple(@NotNull final String tupleId, @NotNull final String nodeId, String fragmentId)
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

    public final String insertTuple(@NotNull final List<String> tupleIds, @NotNull final String nodeId, String fragmentId){
        _commandExecutionLock.lock();
        try {
            throw new UnsupportedOperationException();
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    private final String verifyAndRunInsertTuple(@NotNull final List<String> tupleIds, @NotNull final String nodeId, String fragmentId)
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

    public final String deleteTuple(@NotNull final String tupleId){
        _commandExecutionLock.lock();
        try {
            return runDeleteTuple(tupleId);
        }
        finally {
            _commandExecutionLock.unlock();
        }
    }

    public final String formFragment(@NotNull final List<String> tupleIds, final String fragmentId)
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

    public final String appendTupleToFragment(@NotNull final String tupleId, @NotNull final String fragmentId)
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

    public final String replicateFragment(@NotNull final String fragmentId,
                                    @NotNull final String sourceNodeId,
                                    @NotNull final String destinationNodeId)
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

    public final String deleteFragmentExemplar(@NotNull final String fragmentId, @NotNull final String nodeId)
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

    public final String deleteFragmentCompletely(@NotNull final String fragmentId)
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
    protected abstract String runInsertTuple(@NotNull final String tupleId, @NotNull final String nodeId, String fragmentId);

    /**
     * Bulk insertion (intended to initial data population mostly)
     * @param tupleId
     * @param nodeId
     * @param fragmentId
     * @return
     */
    protected abstract String runInsertTuple(@NotNull final List<String> tupleId, @NotNull final String nodeId, String fragmentId);

    /**
     * Deleter tuple and exclude from all of the fragments it's in
     * @param tupleId
     * @return
     */
    protected abstract String runDeleteTuple(@NotNull final String tupleId);

    /**
     * Create fragment including the specified tuples. A tuple can belong to only one fragment at a time
     * @param tupleId
     * @return
     */
    protected abstract String runFormFragment(@NotNull final List<String> tupleId, String fragmentId);

    /**
     * Attach tuple to a fragment. If a fragment is already part of a tuple, exclude it from the current fragment.
     * @param tupleId Tuple Id that should be added to the specified fragment
     * @param fragmentId Destination fragment Id
     * @return
     */
    protected abstract String runAppendTupleToFragment(@NotNull final String tupleId, @NotNull final String fragmentId);

    /**
     * Create a copy of a fragment
     * @param fragmentId
     * @param sourceNodeId
     * @param destinationNodeId
     * @return
     */
    protected abstract String runReplicateFragment(@NotNull final String fragmentId,
                                                   @NotNull final String sourceNodeId,
                                                   @NotNull final String destinationNodeId);

    /**
     * Remove replicated copy of a fragment, if a fragment it not
     * @param fragmentId Fragment Id
     * @param nodeId Node from which the fragment must be erased
     * @return
     */
    protected abstract String runDeleteFragmentExemplar(@NotNull final String fragmentId, @NotNull final String nodeId);

    /**
     * **Destructive operation**, removes the fragment completely from the registry.
     * @param fragmentId
     * @return
     */
    protected abstract String runCompleteFragmentDeletion(@NotNull final String fragmentId);

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
        DESTROY_FRAGMENT("ERASE");

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

    public final String ARG_TUPLE_ID = "Tuple Id";
    public final String ARG_TUPLE_IDs = "Tuple Ids";
    public final String ARG_NODE_ID = "Node Id";
    public final String ARG_NODE_ID_B = "Node Id B";
    public final String ARG_FRAGMENT_ID = "Fragment Id";
}
