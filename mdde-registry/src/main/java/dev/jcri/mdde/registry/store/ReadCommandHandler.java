package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import dev.jcri.mdde.registry.store.response.FullRegistry;
import dev.jcri.mdde.registry.store.response.serialization.IResponseSerializer;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ReadCommandHandler<T> {

    IResponseSerializer<T> _serializer;
    public ReadCommandHandler(IResponseSerializer<T> serializer){
        Objects.requireNonNull(serializer, "Response serialization handler is not supplied ");
        _serializer = serializer;
    }

    public T runCommand(Commands readCommand, Map<String, Object> arguments)
            throws UnknownRegistryCommandExceptions, ResponseSerializationException, ReadOperationException {
        switch (readCommand)    {
            case GET_REGISTRY:
                return processGetFullRegistryCommand();
            case FIND_TUPLE:
                return processFindTupleCommand(arguments);
            case FIND_TUPLE_FRAGMENT:
                return processFindTupleFragmentCommand(arguments);
            case FIND_FRAGMENT:
                return processFindFragmentNodesCommand(arguments);
            case GET_FRAGMENT_TUPLES:
                return processGetFragmentTuplesCommand(arguments);
            case COUNT_FRAGMENT:
                return processCountFragmentsCommand(arguments);
            case COUNT_TUPLE:
                return processCountTuplesCommand(arguments);
            case GET_NODES:
                return processGetNodesCommand();
        }

        throw new UnknownRegistryCommandExceptions(readCommand.toString());
    }

    private T processGetFullRegistryCommand() throws ResponseSerializationException, ReadOperationException {
        return _serializer.serialize(getFullRegistry());
    }

    private T processFindTupleCommand(Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FIND_TUPLE.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.FIND_TUPLE.toString(), ARG_TUPLE_ID));

        return _serializer.serialize(getTupleNodes(tupleId));
    }

    private T processFindTupleFragmentCommand(Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FIND_TUPLE_FRAGMENT.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.FIND_TUPLE.toString(), ARG_TUPLE_ID));

        return _serializer.serialize(getTupleFragment(tupleId));
    }

    private T processFindFragmentNodesCommand(Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return _serializer.serialize(getFragmentNodes(fragmentId));
    }

    private T processGetFragmentTuplesCommand(Map<String, Object> arguments)
            throws ResponseSerializationException, ReadOperationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return _serializer.serialize(getFragmentTuples(fragmentId));
    }

    private T processCountFragmentsCommand(Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return _serializer.serialize(getCountFragment(fragmentId));
    }

    private T processCountTuplesCommand(Map<String, Object> arguments)
            throws ResponseSerializationException {
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.FIND_TUPLE.toString(), ARG_TUPLE_ID));

        return _serializer.serialize(getCountTuple(tupleId));
    }

    private T processGetNodesCommand()
            throws ResponseSerializationException {
        var nodesList = getNodes();
        return _serializer.serialize(nodesList);
    }

    /**
     * Retrieve a snapshot of the current state for the entire registry including tuples, nodes and fragments
     * @return Current registry snapshot
     */
    public abstract FullRegistry getFullRegistry() throws ReadOperationException;

    /**
     * Get the list of node Ids where the tuple is located
     * @param tupleId Tuple ID
     * @return list of nodes where the specific tuple can be found
     */
    public abstract Set<String> getTupleNodes(final String tupleId);

    /**
     * Get the id of a fragment to which the tuple belongs
     * @param tupleId Tuple ID
     * @return Fragment ID
     */
    public abstract String getTupleFragment(final String tupleId);

    /**
     * Get the list of node IDs where the fragment is located
     * @param fragmentId Fragment ID
     * @return List of Node IDs
     */
    public abstract Set<String> getFragmentNodes(final String fragmentId);

    /**
     * Get all of the fragments stored within the node
     * @param nodeId Node ID
     * @return Set of fragments mapped to the node
     */
    public abstract Set<String> getNodeFragments(final String nodeId);

    /**
     * Get the list of all tuple IDs that belong to the fragment
     * @param fragmentId Fragment ID
     * @return List of Tuple IDs
     */
    public abstract Set<String> getFragmentTuples(final String fragmentId) throws ReadOperationException;

    /**
     * Get the number of instances of a specific fragment
     * @param fragmentId Fragment ID
     * @return Count of the specified fragment instances across all nodes
     */
    public abstract int getCountFragment(final String fragmentId);

    /**
     * Get the number of instances of a specific tuple
     * @param tupleId Tuple ID
     * @return Count of the specified tuple instances across all nodes
     */
    public abstract int getCountTuple(final String tupleId);

    /**
     * Get the list of all node IDs
     * @return List of all node IDs that are present in the registry
     */
    public abstract Set<String> getNodes();

    /**
     * Check if the specified node ID is registered within the registry
     * @param nodeId Node ID
     * @return True if node exists, False otherwise
     */
    public abstract boolean getIsNodeExists(final String nodeId);
    /**
     * Check if the specified tuple ID is registered within the registry
     * @param tupleId Tuple ID
     * @return True if tuple exists, False otherwise
     */
    public boolean getIsTupleExists(final String tupleId){
        return getCountFragment(tupleId) > 0;
    }
    /**
     * Check if the specified fragment ID is registered within the registry
     * @param fragmentId Fragment ID
     * @return True if fragment exists, False otherwise
     */
    public boolean getIsFragmentExists(final String fragmentId){
        return getCountFragment(fragmentId) > 0;
    }

    /**
     * Get a set of tuples not assigned to a fragment, located on a specific node
     * @param nodeId Node ID
     * @return Set of Tuple IDs
     */
    public abstract Set<String> getUnassignedTuples(String nodeId);

    /**
     * Check of the specific tuple is unassigned on a specific node
     * @param nodeId Tuple ID
     * @param tupleIds Node ID
     * @return True - tuple is unassigned
     */
    public abstract Boolean getIsTuplesUnassigned(String nodeId, Set<String> tupleIds);

    /**
     * Check if node contains fragment
     * @param nodeId Node ID
     * @param fragmentId Fragment ID
     * @return True - specified fragment is located within the specified node
     */
    public abstract Boolean getIsNodeContainsFragment(String nodeId, String fragmentId);

    /**
     * Get the set of all fragments IDs
     * @return All registered fragment IDs
     */
    public abstract Set<String> getAllFragmentIds();

    /**
     * Catalog of the available READ operations
     */
    public enum Commands {
        GET_REGISTRY("GETALL"),
        FIND_TUPLE("FINDTUPLE"),
        FIND_TUPLE_FRAGMENT("TUPLEFRAGMENT"),
        FIND_FRAGMENT("FINDFRAGMENT"),
        GET_FRAGMENT_TUPLES("GETFRAGTUPLES"),
        COUNT_FRAGMENT("COUNTFRAGMENT"),
        COUNT_TUPLE("COUNTTUPLE"),
        GET_NODES("NODES");

        private final String _command;

        /**
         * @param command
         */
        Commands(final String command) {
            this._command = command;
        }

        private static Map<String, Commands> _commandsMap = Arrays.stream(Commands.values()).collect(Collectors.toMap(e -> e._command, e -> e));

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
    public final String ARG_FRAGMENT_ID = "Fragment Id";
    public final String ARG_NODE_ID = "Node Id";
}
