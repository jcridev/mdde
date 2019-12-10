package dev.jcri.mdde.registry.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import dev.jcri.mdde.registry.store.response.FullRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ReadCommandHandler {

    public String runCommand(Commands readCommand, Map<String, Object> arguments)
            throws JsonProcessingException, UnknownRegistryCommandExceptions {
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

    private String processGetFullRegistryCommand(){
        return getFullRegistry().toString();
    }

    private String processFindTupleCommand(@NotNull Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FIND_TUPLE.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.FIND_TUPLE.toString(), ARG_TUPLE_ID));

        return getTupleNodes(tupleId);
    }

    private String processFindTupleFragmentCommand(@NotNull Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                Commands.FIND_TUPLE_FRAGMENT.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.FIND_TUPLE.toString(), ARG_TUPLE_ID));

        return getTupleFragment(tupleId);
    }

    private String processFindFragmentNodesCommand(@NotNull Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return getFragmentNodes(fragmentId);
    }

    private String processGetFragmentTuplesCommand(@NotNull Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return getFragmentTuples(fragmentId);
    }

    private String processCountFragmentsCommand(@NotNull Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var fragmentId = (String) Objects.requireNonNull(arguments.get(ARG_FRAGMENT_ID), String.format("%s must be invoked with %s",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString(), ARG_FRAGMENT_ID));

        return Integer.toString(getCountFragment(fragmentId));
    }

    private String processCountTuplesCommand(@NotNull Map<String, Object> arguments){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments",
                WriteCommandHandler.Commands.FORM_FRAGMENT.toString()));

        var tupleId = (String) Objects.requireNonNull(arguments.get(ARG_TUPLE_ID), String.format("%s must be invoked with %s",
                Commands.FIND_TUPLE.toString(), ARG_TUPLE_ID));

        return Integer.toString(getCountTuple(tupleId));
    }

    private String processGetNodesCommand()
            throws JsonProcessingException {
        var nodesList = getNodes();

        // TODO: Convert output to dedicatedClass
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(nodesList);
    }

    public abstract FullRegistry getFullRegistry();

    public abstract String getTupleNodes(@NotNull final String tupleId);

    public abstract String getTupleFragment(@NotNull final String tupleId);

    public abstract String getFragmentNodes(@NotNull final String fragmentId);

    public abstract String getFragmentTuples(@NotNull final String fragmentId);

    public abstract int getCountFragment(@NotNull final String fragmentId);

    public abstract int getCountTuple(@NotNull final String tupleId);

    public abstract List<String> getNodes();

    public abstract boolean getIsNodeExists(@NotNull final String nodeId);

    public boolean getIsTupleExists(@NotNull final String tupleId){
        return getCountFragment(tupleId) > 0;
    }

    public boolean getIsFragmentExists(@NotNull final String fragmentId){
        return getCountFragment(fragmentId) > 0;
    }

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
