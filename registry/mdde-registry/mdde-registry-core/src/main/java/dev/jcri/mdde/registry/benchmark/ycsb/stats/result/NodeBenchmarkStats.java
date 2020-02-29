package dev.jcri.mdde.registry.benchmark.ycsb.stats.result;

import java.util.Collection;

public class NodeBenchmarkStats {
    private final String _nodeId;
    private final Collection<FragmentBenchmarkStats> _fragments;

    /**
     * Constructor
     * @param fragmentsStats Fragments contained within the node
     */
    public NodeBenchmarkStats(String nodeId, Collection<FragmentBenchmarkStats> fragmentsStats){
        _fragments = fragmentsStats;
        _nodeId = nodeId;
    }

    /**
     * Fragments contained within the node
     * @return List of fragments with the statistics
     */
    public Collection<FragmentBenchmarkStats> getFragments() {
        return _fragments;
    }

    public String getNodeId() {
        return _nodeId;
    }
}
