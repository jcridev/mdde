package dev.jcri.mdde.registry.shared.commands.containers.args;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.jcri.mdde.registry.shared.commands.Constants;

import java.util.Set;

/**
 * ADDNODES argument
 */
public class WriteArgsPopulateNodesContainer {

    private Set<String> nodes;

    @JsonGetter(Constants.ArgNodeIdsField)
    public Set<String> getNodes() {
        return nodes;
    }
    @JsonSetter(Constants.ArgNodeIdsField)
    public void setNodes(Set<String> nodes) {
        this.nodes = nodes;
    }
}
