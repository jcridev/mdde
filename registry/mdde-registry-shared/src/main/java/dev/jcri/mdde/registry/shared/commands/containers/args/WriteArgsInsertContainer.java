package dev.jcri.mdde.registry.shared.commands.containers.args;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.jcri.mdde.registry.shared.commands.Constants;

/**
 * INSERT_TUPLE arguments
 */
public class WriteArgsInsertContainer {
    private String tupleId;
    private String nodeId;

    @JsonGetter(Constants.ArgTupleIdField)
    public String getTupleId() {
        return tupleId;
    }
    @JsonSetter(Constants.ArgTupleIdField)
    public void setTupleId(String tupleId) {
        this.tupleId = tupleId;
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
