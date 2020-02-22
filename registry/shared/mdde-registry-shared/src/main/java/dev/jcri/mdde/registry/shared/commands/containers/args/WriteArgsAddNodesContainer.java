package dev.jcri.mdde.registry.shared.commands.containers.args;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.jcri.mdde.registry.shared.commands.Constants;

import java.util.Set;

/**
 * POPULATE_NODES arguments
 */
public class WriteArgsAddNodesContainer {
    private Set<String> nodeIds;

    @JsonGetter(Constants.ArgNodeIdsField)
    public Set<String> getNodeId() {
        return nodeIds;
    }
    @JsonSetter(Constants.ArgNodeIdsField)
    public void setNodeId(Set<String> nodeId) {
        this.nodeIds = nodeId;
    }
}
