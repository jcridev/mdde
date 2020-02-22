package dev.jcri.mdde.registry.shared.commands;


import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;

/**
 * Registry manipulation commands available
 */
public enum EWriteCommand implements ICommand {
    /**
     * Insert a new tuple ID into the registry
     */
    INSERT_TUPLE("INSERT",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_TUPLE_ID); add(ARG_NODE_ID);}
            }),
    /**
     * Insert a set of tuple IDs into the registry
     */
    INSERT_TUPLE_BULK("INSERTMANY",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_TUPLE_IDs); add(ARG_NODE_ID);}
            }),
    /**
     * Remove tuple ID from the registry
     */
    DELETE_TUPLE("DELTUPLE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_TUPLE_ID); }}),
    /**
     * Create a new fragment
     */
    FORM_FRAGMENT("FRAGMAKE",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_TUPLE_IDs); add(ARG_FRAGMENT_ID);}
            }),
    /**
     * Append tuple to an existing fragment
     */
    APPEND_TO_FRAGMENT("FRAGAPPEND",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_TUPLE_ID); add(ARG_FRAGMENT_ID);}
            }),
    /**
     * Copy an existing fragment from one data node to another.
     * The action affects both the registry and data nodes
     */
    REPLICATE_FRAGMENT_DATA("DCOPYFRAG",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_FRAGMENT_ID); add(ARG_NODE_ID); add(ARG_NODE_ID_B);}
            }),
    /**
     * Delete an exemplar of a fragment from a specific node
     * The action affects both the registry and data nodes
     */
    DELETE_FRAGMENT_DATA("DDELFRAGEX",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_FRAGMENT_ID); add(ARG_NODE_ID);}
            }),
    /**
     * Completely deleter the fragment from the registry
     */
    DESTROY_FRAGMENT("ERASEFRAG",
            new ArrayList<ExpectedCommandArgument>(){
        {add(ARG_FRAGMENT_ID);}
    }),
    /**
     * Add a set of nodes IDs to the registry
     */
    POPULATE_NODES("ADDNODES", new ArrayList<ExpectedCommandArgument>(){{add(ARG_NODE_IDs);}}),

    META_FRAGMENT_EXEMPLAR("PUTMETAFRAGEXM",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_FRAGMENT_ID); add(ARG_NODE_ID); add(ARG_FRAGMENT_META_TAG); add(ARG_FRAGMENT_META_VALUE);}
            }),

    META_FRAGMENT_GLOBAL("PUTMETAFRAGGLB",
            new ArrayList<ExpectedCommandArgument>(){
                {add(ARG_FRAGMENT_ID); add(ARG_FRAGMENT_META_TAG); add(ARG_FRAGMENT_META_VALUE);}
            });


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
