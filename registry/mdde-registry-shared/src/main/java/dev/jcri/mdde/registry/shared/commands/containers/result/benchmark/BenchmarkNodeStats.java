package dev.jcri.mdde.registry.shared.commands.containers.result.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;


public class BenchmarkNodeStats {
    public static final String NODE_ID_FIELD = "nodeId";
    public static final String READS_FIELD = "reads";
    /**
     * Node ID
     */
    private String nodeId;
    /**
     * Total # of reads from this node during the benchmark run
     */
    private long reads;

    @JsonGetter(NODE_ID_FIELD)
    public String getNodeId() {
        return nodeId;
    }
    @JsonSetter(NODE_ID_FIELD)
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @JsonGetter(READS_FIELD)
    public long getReads() {
        return reads;
    }
    @JsonSetter(READS_FIELD)
    public void setReads(long reads) {
        this.reads = reads;
    }
}
