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
     * Prefix for the specific key storing unassigned to any fragment tuples stored on a node
     */
    public static final String NODE_HEAP = "mdde/unassigned/";

    /**
     * Private default constructor preventing creation of instances
     */
    private Constants(){};
}