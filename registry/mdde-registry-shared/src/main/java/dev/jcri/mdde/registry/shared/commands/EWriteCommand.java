package dev.jcri.mdde.registry.shared.commands;


import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;

/**
 * Registry manipulation commands available
 */
public enum EWriteCommand implements ICommand {
    INSERT_TUPLE("INSERT", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID); add(ARG_NODE_ID);}}),
    INSERT_TUPLE_BULK("INSERTMANY", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_IDs); add(ARG_NODE_ID);}}),
    DELETE_TUPLE("DELTUPLE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID); }}),
    FORM_FRAGMENT("GROUP", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_IDs); add(ARG_FRAGMENT_ID); add(ARG_NODE_ID);}}),
    APPEND_TO_FRAGMENT("APPEND", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID); add(ARG_FRAGMENT_ID);}}),
    REPLICATE_FRAGMENT("REPLICATE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID); add(ARG_NODE_ID); add(ARG_NODE_ID_B);}}),
    DELETE_FRAGMENT("DELFRAGCOPY", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID); add(ARG_NODE_ID);}}),
    DESTROY_FRAGMENT("ERASE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID);}}),
    POPULATE_NODES("ADDNODES", new ArrayList<ExpectedCommandArgument>(){{add(ARG_NODE_IDs);}});

    private final String _command;
    private final List<ExpectedCommandArgument> _expectedArguments;
    private final int _numExpectedArguments;

    /**
     * Constructor
     * @param command Command tag
     * @param args List of expected arguments,in the order they should arrive
     */
    EWriteCommand(final String command, final List<ExpectedCommandArgument> args) {
        Objects.requireNonNull(args);
        this._command = command;
        _expectedArguments = args;
        _numExpectedArguments = _expectedArguments.size();
    }

    private static Map<String, EWriteCommand> _commandsMap = Arrays.stream(EWriteCommand.values())
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
     * @throws NoSuchElementException
     */
    public static EWriteCommand getCommandTag(String tag) throws NoSuchElementException {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        EWriteCommand command = _commandsMap.get(tag);
        if(command == null){
            throw new NoSuchElementException(tag);
        }
        return command;
    }
}
