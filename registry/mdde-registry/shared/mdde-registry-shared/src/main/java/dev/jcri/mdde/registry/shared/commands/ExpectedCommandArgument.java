package dev.jcri.mdde.registry.shared.commands;

/**
 * Class containing the description of an argument expected by a command
 */
public enum ExpectedCommandArgument{
    ARG_TUPLE_ID("Tuple ID",
            Constants.ArgTupleIdField, ArgumentType.STRING),
    ARG_TUPLE_IDs("Tuple IDs",
            Constants.ArgTupleIdsField, ArgumentType.SET_STRINGS),
    ARG_NODE_ID("Node ID",
            Constants.ArgNodeIdField, ArgumentType.STRING),
    ARG_NODE_IDs("Node IDs",
            Constants.ArgNodeIdsField, ArgumentType.SET_STRINGS),
    ARG_NODE_ID_B("2nd Node ID", // Typically used as a destination node (example: COPY)
            Constants.ArgSecondNodeIdFiled, ArgumentType.STRING),
    ARG_FRAGMENT_ID("Fragment ID",
            Constants.ArgFragmentIdField, ArgumentType.STRING),

    ARG_FRAGMENT_META_TAG("Fragment meta tag",
            Constants.ArgFragmentMetaTag, ArgumentType.STRING),

    ARG_FRAGMENT_META_TAGS_LOCAL("Set of local meta tags",
            Constants.ArgFragmentLocalMetaTags, ArgumentType.SET_STRINGS),
    ARG_FRAGMENT_META_TAGS_GLOBAL("Set of global meta tags",
            Constants.ArgFragmentGlobalMetaTags, ArgumentType.SET_STRINGS),

    ARG_FRAGMENT_META_VALUE("Fragment meta value",
            Constants.ArgFragmentMetaValue, ArgumentType.STRING),

    ARG_WORKLOAD_ID("Benchmark workload ID",
            Constants.ArgWorkloadIdField, ArgumentType.STRING),
    ARG_WORKLOAD_WORKERS("Number of YCSB workers",
            Constants.ArgWorkloadWorkersField, ArgumentType.INTEGER),
    ARG_DEFAULT_SNAPSHOT_FLAG("Snapshot ID assigned as default",
            Constants.ArgSnapshotDefaultField, ArgumentType.BOOLEAN),
    ARG_SNAPSHOT_ID("Snapshot ID",
            Constants.ArgSnapshotIdField, ArgumentType.STRING),

    ARG_BENCH_COUNTERFEIT_ADJUSTER("Adjustment of magnitude for the estimated benchmark",
            Constants.ArgBenchmarkCounterfeitMagnitude, ArgumentType.DOUBLE);

    private final String _title;
    private final ArgumentType _argumentType;
    private final String _tag;

    /**
     * Constructor
     * @param title Argument title, should be unique
     * @param argumentType Argument type
     * @param tag Tag
     */
    ExpectedCommandArgument(String title, String tag, ArgumentType argumentType) {
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
        INTEGER("Integer"),
        DOUBLE("Double"),
        SET_STRINGS("Set of strings"),
        BOOLEAN("Boolean");

        private final String _description;
        ArgumentType(String description){
            _description = description;
        }

        /**
         * Argument type description
         * @return String description
         */
        public String getDescription() {
            return _description;
        }

        @Override
        public String toString() {
            return _description;
        }
    }

    /**
     * Returns argument description title
     * @return Tag, type, tile
     */
    @Override
    public String toString() {
        return  this.getTag() + "[" + this.getArgumentType() +"] - " + this.getTitle();
    }

    /**
     * Unique argument title
     * @return Argument title
     */
    public String getTitle(){
        return _title;
    }

    /**
     * Expected type of the argument
     * @return Argument type description
     */
    public ArgumentType getArgumentType(){
        return _argumentType;
    }

    /**
     * Get argument tag used in serialization and deserialization
     * @return Tag that should be used in the command serialization (e.g. JSON, XML)
     */
    public String getTag(){
        return _tag;
    }
}
