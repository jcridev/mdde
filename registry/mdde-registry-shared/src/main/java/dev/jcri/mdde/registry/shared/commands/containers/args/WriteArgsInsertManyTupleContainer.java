package dev.jcri.mdde.registry.shared.commands.containers.args;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.jcri.mdde.registry.shared.commands.Constants;

import java.util.Set;

/**
 * INSERTMANY arguments
 */
public class WriteArgsInsertManyTupleContainer {
    private Set<String> tupleIds;
    private String nodeId;

    @JsonGetter(Constants.ArgTupleIdsField)
    public Set<String> getTupleIds() {
        return tupleIds;
    }
    @JsonSetter(Constants.ArgTupleIdsField)
    public void setTupleIds(Set<String> nodeId) {
        this.tupleIds = nodeId;
    }
    @JsonGetter(Constants.ArgNodeIdField)
    public String getNodeId() {
        return nodeId;
    }
    @JsonSetter(Constants.ArgNodeIdField)
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
