package dev.jcri.mdde.registry.shared.commands;

/**
 * Class containing the description of an argument expected by a command
 */
public enum ExpectedCommandArgument{
    ARG_TUPLE_ID("Tuple ID", Constants.ArgTupleIdField, ExpectedCommandArgument.ArgumentType.STRING),
    ARG_TUPLE_IDs("Tuple IDs", Constants.ArgTupleIdsField, ExpectedCommandArgument.ArgumentType.SET_STRINGS),
    ARG_NODE_ID("Node ID", Constants.ArgNodeIdField, ExpectedCommandArgument.ArgumentType.STRING),
    ARG_NODE_IDs("Node IDs", Constants.ArgNodeIdsField, ExpectedCommandArgument.ArgumentType.SET_STRINGS),
    ARG_NODE_ID_B("2nd Node ID", Constants.ArgSecondNodeIdFiled, ExpectedCommandArgument.ArgumentType.STRING),
    ARG_FRAGMENT_ID("Fragment ID", Constants.ArgFragmentIdField, ExpectedCommandArgument.ArgumentType.STRING),

    ARG_WORKLOAD_ID("Benchmark workload ID", Constants.ArgWorkloadIdField, ExpectedCommandArgument.ArgumentType.STRING),
    ARG_DEFAULT_SNAPSHOT_FLAG("Snapshot ID assigned as default", Constants.ArgSnapshotDefaultField, ArgumentType.BOOLEAN),
    ARG_SNAPSHOT_ID("Snapshot ID", Constants.ArgSnapshotIdField, ArgumentType.STRING);

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
        STRING("String"),
        SET_STRINGS("Set of strings"),
        BOOLEAN("Boolean");

        private final String _description;
        ArgumentType(String description){
            _description = description;
        }

        public String getDescription() {
            return _description;
        }

        @Override
        public String toString() {
            return _description;
        }
    }

    /**
     * Returns argument title
     * @return
     */
    @Override
    public String toString() {
        return  this.getTag() + "[" + this.getArgumentType() +"] - " + this.getTitle();
    }

    public String getTitle(){
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
