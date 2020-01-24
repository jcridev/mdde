package dev.jcri.mdde.registry.shared.commands;

import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;

/**
 * Enum of the available READ commands
 */
public enum EReadCommand implements ICommand {
    GET_REGISTRY("GETALL", new ArrayList<ExpectedCommandArgument>()),
    FIND_TUPLE("FINDTUPLE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID);}}),
    FIND_TUPLE_FRAGMENT("TUPLEFRAGMENT", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID);}}),
    FIND_FRAGMENT("FINDFRAGMENT", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID);}}),
    GET_FRAGMENT_TUPLES("GETFRAGTUPLES", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID);}}),
    COUNT_FRAGMENT("COUNTFRAGMENT", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID);}}),
    COUNT_TUPLE("COUNTTUPLE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID);}}),
    GET_NODES("NODES", new ArrayList<>());

    private final String _command;
    private final List<ExpectedCommandArgument> _expectedArguments;
    private final int _numExpectedArguments;

    /**
     * @param command Command keyword for the processor
     */
    EReadCommand(final String command, final List<ExpectedCommandArgument> args) {
        Objects.requireNonNull(args);
        this._command = command;
        _expectedArguments = args;
        _numExpectedArguments = _expectedArguments.size();
    }

    private static Map<String, EReadCommand> _commandsMap = Arrays.stream(EReadCommand.values())
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
     * Get command by its textual tag
     * @param tag Command tag (ex. 'GETALL')
     * @return
     * @throws NoSuchElementException
     */
    public static EReadCommand getCommandTag(String tag) throws NoSuchElementException {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        EReadCommand command = _commandsMap.get(tag);
        if(command == null){
            throw new NoSuchElementException(tag);
        }
        return command;
    }
}
