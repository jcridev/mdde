package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.control.ExpectedCommandArgument.*;

public enum ReadCommands implements ICommands{
    GET_REGISTRY("GETALL", new ArrayList<>()),
    FIND_TUPLE("FINDTUPLE", new ArrayList<>(){{add(ARG_TUPLE_ID);}}),
    FIND_TUPLE_FRAGMENT("TUPLEFRAGMENT", new ArrayList<>(){{add(ARG_FRAGMENT_ID);}}),
    FIND_FRAGMENT("FINDFRAGMENT", new ArrayList<>(){{add(ARG_FRAGMENT_ID);}}),
    GET_FRAGMENT_TUPLES("GETFRAGTUPLES", new ArrayList<>(){{add(ARG_FRAGMENT_ID);}}),
    COUNT_FRAGMENT("COUNTFRAGMENT", new ArrayList<>(){{add(ARG_FRAGMENT_ID);}}),
    COUNT_TUPLE("COUNTTUPLE", new ArrayList<>(){{add(ARG_TUPLE_ID);}}),
    GET_NODES("NODES", new ArrayList<>());

    private final String _command;
    private final List<ExpectedCommandArgument> _expectedArguments;
    private final int _numExpectedArguments;

    /**
     * @param command Command keyword for the processor
     */
    ReadCommands(final String command, final List<ExpectedCommandArgument> args) {
        this._command = command;
        _expectedArguments = args;
        if(_expectedArguments == null){
            _numExpectedArguments = 0;
        }
        else {
            _numExpectedArguments = _expectedArguments.size();
        }
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

    /**
     * Number of arguments that are expected by a specific command
     * @return
     */
    public int getNumberOfExpectedArguments(){
        return _numExpectedArguments;
    }

    /**
     * Get a list of the arguments expected by the command
     * @return
     */
    public List<ExpectedCommandArgument> getExpectedArguments(){
        return Collections.unmodifiableList(_expectedArguments);
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
