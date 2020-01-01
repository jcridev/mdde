package dev.jcri.mdde.registry.configuration.redis;


import dev.jcri.mdde.registry.configuration.IDataNode;

/**
 * Redis powered node configuration
 */
public class DataNodeConfigRedis implements IDataNode {
    private String _nodeId = null;

    public String getNodeId() {
        return _nodeId;
    }

    public void setNodeId(String nodeId) {
        this._nodeId = nodeId;
    }

}
