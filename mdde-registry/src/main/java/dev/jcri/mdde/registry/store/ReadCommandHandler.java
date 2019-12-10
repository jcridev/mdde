package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ReadCommandHandler {

    public String runCommand(Commands readCommand){
        return null;
    }

    public abstract int getCountFragment(@NotNull final String fragmentId);

    public abstract boolean getIsFragmentExists(@NotNull final String fragmentId);

    public abstract int getCountTuple(@NotNull final String tupleId);

    public abstract boolean getIsTupleExists(@NotNull final String tupleId);

    public abstract boolean getIsNodeExists(@NotNull final String nodeId);


    public enum Commands {
        GET_REGISTRY("GETALL"),
        FIND_TUPLE("FINDTUPLE"),
        FIND_TUPLE_FRAGMENT("TUPLEFRAGMENT"),
        FIND_FRAGMENT("FINDFRAGMENT"),
        GET_FRAGMENT_TUPLES("GETFRAGTUPLES"),
        COUNT_FRAGMENT("COUNTFRAGMENT"),
        COUNT_TUPLE("COUNTTUPLE");

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
    public final String ARG_TUPLE_IDs = "Tuple Ids";
    public final String ARG_NODE_ID = "Node Id";
}
