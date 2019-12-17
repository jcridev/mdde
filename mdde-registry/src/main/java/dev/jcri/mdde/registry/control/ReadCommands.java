package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ReadCommands implements ICommands{
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
     * @param command Command keyword for the processor
     */
    ReadCommands(final String command) {
        this._command = command;
    }

    private static Map<String, ReadCommands> _commandsMap = Arrays.stream(ReadCommands.values()).collect(Collectors.toMap(e -> e._command, e -> e));

    @Override
    public String toString() {
        return _command;
    }

    /**
     * Get the command text
     * @return
     */
    public String getCommand(){
        return _command;
    }

    public static ReadCommands getCommandTag(String tag) throws UnknownRegistryCommandExceptions {
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
