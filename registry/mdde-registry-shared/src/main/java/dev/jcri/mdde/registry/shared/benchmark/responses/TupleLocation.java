package dev.jcri.mdde.registry.shared.benchmark.responses;

/**
 * Location of the tuple
 */
public class TupleLocation {
    /**
     * Node ID where the tuple should be read from.
     * Null if the Tuple ID wasn't found in the registry.
     */
    protected String nodeId;

    /**
     * Default constructor
     */
    public TupleLocation(){}

    /**
     * Constructor
     * @param nodeId If tuple is was found return the node ID where it should be read from.
     */
    public TupleLocation(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Node ID from where the tuple should be read.
     * @return Node ID or null of the tuple was not located in the registry.
     */
    public String getNodeId() {
        return nodeId;
    }

    public boolean tupleExists(){
        return nodeId != null && !nodeId.isEmpty();
    }
}
