package dev.jcri.mdde.registry.shared.commands.containers.result.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Collection;
import java.util.List;


public class BenchmarkNodeStats {
    public static final String NODE_ID_FIELD = "nodeId";
    public static final String FRAGMENTS_FIELD = "frags";
    /**
     * Node ID
     */
    private String _nodeId;
    /**
     * Total # of reads from this node during the benchmark run
     */
    private Collection<BenchmarkFragmentStats> _fragments;

    /**
     * Default constructor
     */
    public BenchmarkNodeStats(){};

    public BenchmarkNodeStats(String nodeId, Collection<BenchmarkFragmentStats> fragments){
        _nodeId = nodeId;
        _fragments = fragments;
    }

    @JsonGetter(NODE_ID_FIELD)
    public String getNodeId() {
        return _nodeId;
    }
    @JsonSetter(NODE_ID_FIELD)
    public void setNodeId(String nodeId) {
        this._nodeId = nodeId;
    }

    @JsonGetter(FRAGMENTS_FIELD)
    public Collection<BenchmarkFragmentStats> getFragments() {
        return _fragments;
    }
    @JsonSetter(FRAGMENTS_FIELD)
    public void setFragments(List<BenchmarkFragmentStats> reads) {
        this._fragments = reads;
    }

}
