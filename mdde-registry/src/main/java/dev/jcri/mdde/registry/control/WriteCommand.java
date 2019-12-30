package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.control.ExpectedCommandArgument.*;

/**
 * Registry manipulation commands available
 */
public enum WriteCommand implements ICommand {
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
    WriteCommand(final String command, final List<ExpectedCommandArgument> args) {
        this._command = command;
        _expectedArguments = args;
        if(_expectedArguments == null){
            _numExpectedArguments = 0;
        }
        else {
            _numExpectedArguments = _expectedArguments.size();
        }
    }

    private static Map<String, WriteCommand> _commandsMap = Arrays.stream(WriteCommand.values())
            .collect(Collectors.toMap(e -> e._command, e -> e));

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

    /**
     * Get specific command by the text key tag
     * @param tag Tag
     * @return
     * @throws UnknownRegistryCommandExceptions
     */
    public static WriteCommand getCommandTag(String tag) throws UnknownRegistryCommandExceptions {
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
