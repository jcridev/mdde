package dev.jcri.mdde.registry.configuration;


/**
 * Redis powered node configuration
 */
public class RedisNodeConfig implements IDataNode {
    private String _nodeId = null;

    public String getNodeId() {
        return _nodeId;
    }

    public void setNodeId(String nodeId) {
        this._nodeId = nodeId;
    }

}
