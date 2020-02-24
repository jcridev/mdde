package dev.jcri.mdde.registry.shared.commands;

import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;

/**
 * Enum of the available READ commands
 */
public enum EReadCommand implements ICommand {
    /**
     * Get overview of the entire registry
     */
    GET_REGISTRY("GETALL", new ArrayList<>()),
    /**
     * Get nodes where the specific tuple is located
     */
    FIND_TUPLE("FINDTUPLE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID);}}),
    /**
     * Get all tuples that are located on the specific node but not assigned to any fragment
     */
    NODE_TUPLES_UNASSIGNED("NTUPLESU", new ArrayList<ExpectedCommandArgument>(){{add(ARG_NODE_ID);}}),
    /**
     * Get all fragments that are located on the specific node
     */
    NODE_FRAGMENTS("NFRAGS", new ArrayList<ExpectedCommandArgument>(){{add(ARG_NODE_ID);}}),
    /**
     * Find the fragment to which the tuple is assigned
     */
    FIND_TUPLE_FRAGMENT("TUPLEFRAGMENT", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID);}}),
    /**
     * Find all of the nodes where the fragment is located
     */
    FIND_FRAGMENT("FINDFRAGMENT", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID);}}),
    /**
     * Get all of the tuple IDs assigned to the fragment
     */
    GET_FRAGMENT_TUPLES("GETFRAGTUPLES", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID);}}),
    /**
     * Get all fragments with locations, specified meta tags
     */
    GET_ALL_FRAGMENTS_NODES_WITH_META("GETFRAGSWMETA",
            new ArrayList<ExpectedCommandArgument>(){
        {add(ARG_FRAGMENT_META_TAGS_LOCAL); add(ARG_FRAGMENT_META_TAGS_GLOBAL);}
    }),
    /**
     * Get the total number of tuple fragment exemplars
     */
    COUNT_FRAGMENT("COUNTFRAGMENT", new ArrayList<ExpectedCommandArgument>(){{add(ARG_FRAGMENT_ID);}}),
    /**
     * Get the total number of the tuple exemplars
     */
    COUNT_TUPLE("COUNTTUPLE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID);}}),
    /**
     * Get the list of data nodes IDs
     */
    GET_NODES("NODES", new ArrayList<>()),
    /**
     * Get the a meta value assigned to a specific fragment exemplar
     */
    META_FRAGMENT_EXEMPLAR("GETMETAFRAGEXM",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_FRAGMENT_ID); add(ARG_NODE_ID); add(ARG_FRAGMENT_META_TAG);}
            }),
    /**
     * Get the a meta value assigned to the fragment globally
     */
    META_FRAGMENT_GLOBAL("GETMETAFRAGGLB",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_FRAGMENT_ID); add(ARG_FRAGMENT_META_TAG);}
            });

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
