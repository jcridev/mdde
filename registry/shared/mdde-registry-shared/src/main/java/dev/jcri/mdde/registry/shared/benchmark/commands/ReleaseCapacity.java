package dev.jcri.mdde.registry.shared.benchmark.commands;

public class ReleaseCapacity {
    /**
     * Node Id on which capacity should be released
     */
    protected String nodeId;

    /**
     * Default constructor
     */
    public ReleaseCapacity(){}

    /**
     * Constructor
     * @param nodeId Node ID
     */
    public ReleaseCapacity(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId(){
        return nodeId;
    }
}
