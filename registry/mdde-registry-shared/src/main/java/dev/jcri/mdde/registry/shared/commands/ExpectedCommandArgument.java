package dev.jcri.mdde.registry.shared.commands;

/**
 * Class containing the description of an argument expected by a command
 */
public enum ExpectedCommandArgument{
    ARG_TUPLE_ID("Tuple ID", Constants.ArgTupleIdField, ExpectedCommandArgument.ArgumentType.string),
    ARG_TUPLE_IDs("Tuple IDs", Constants.ArgTupleIdsField, ExpectedCommandArgument.ArgumentType.set_strings),
    ARG_NODE_ID("Node ID", Constants.ArgNodeIdField, ExpectedCommandArgument.ArgumentType.string),
    ARG_NODE_IDs("Node IDs", Constants.ArgNodeIdsField, ExpectedCommandArgument.ArgumentType.set_strings),
    ARG_NODE_ID_B("2nd Node ID", Constants.ArgSecondNodeIdFiled, ExpectedCommandArgument.ArgumentType.string),
    ARG_FRAGMENT_ID("Fragment ID", Constants.ArgFragmentIdField, ExpectedCommandArgument.ArgumentType.string);


    private final String _title;
    private final ArgumentType _argumentType;
    private final String _tag;

    /**
     * Constructor
     * @param title Argument title, should be unique
     * @param argumentType Argument type
     * @param tag Tag
     */
    private ExpectedCommandArgument(String title, String tag, ArgumentType argumentType) {
        if(title == null || title.isEmpty()){
            throw new IllegalArgumentException("Argument title can't be null or empty");
        }
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("Argument tag can't be null or empty");
        }

        this._title = title;
        this._argumentType = argumentType;
        this._tag = tag;
    }

    /**
     * Types of the arguments supported
     */
    public enum ArgumentType{
        string,
        set_strings
    }

    /**
     * Returns argument title
     * @return
     */
    @Override
    public String toString() {
        return _title;
    }

    /**
     * Expected type of the argument
     * @return
     */
    public ArgumentType getArgumentType(){
        return _argumentType;
    }

    /**
     * Get argument tag used in serialization and deserialization
     * @return
     */
    public String getTag(){
        return _tag;
    }
}
