package dev.jcri.mdde.registry.store.impl.redis;

/**
 * String constants used in Redis reads and writes
 */
public final class Constants {
    /**
     * Full list of nodes
     */
    public static final String NODES_SET = "mdde/nodes";
    /**
     * Full list of fragments
     */
    public static final String FRAGMENT_SET = "mdde/fragments";
    /**
     * Prefix for the specific key storing a set of fragments assigned to a node
     */
    public static final String NODE_PREFIX = "mdde/node/";
    /**
     * Prefix for the specific key storing a set of tuples that belong to a fragment
     */
    public static final String FRAGMENT_PREFIX = "mdde/fragment/";
    /**
     * Prefix for the key storing global meta data for the specific fragment
     */
    public static final String FRAGMENT_GLOBAL_META_PREFIX = "mdde/fragment/meta/";
    /**
     * Prefix for the key storing specific exemplar located on the specific node
     */
    public static final String FRAGMENT_EXEMPLAR_META_PREFIX = "mdde/fragment/node/meta/";
    /**
     * Prefix for the specific key storing unassigned to any fragment tuples stored on a node
     */
    public static final String NODE_HEAP_PREFIX = "mdde/unassigned/";

    /**
     * Queue (LIST) of the data actions that should be later performed on the actual data nodes
     */
    public static final String DATA_SHUFFLE_QUEUE_KEY = "mdde/_dataqueue";
    /**
     * Key storing the default snapshot ID value. Snapshot with this ID will be used during the RESET call
     */
    public static final String DEFAULT_SNAPSHOT_ID_KEY = "mdde/_snapshot";
    /**
     * Private default constructor preventing creation of instances
     */
    private Constants(){};
}