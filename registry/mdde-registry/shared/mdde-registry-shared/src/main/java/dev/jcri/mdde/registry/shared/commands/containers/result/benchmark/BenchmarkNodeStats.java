package dev.jcri.mdde.registry.shared.commands.containers.result.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public class BenchmarkNodeStats {
    public static final String CLIENT_ID_FIELD = "";
    public static final String NODE_ID_FIELD = "nodeId";
    public static final String FRAGMENTS_FIELD = "frags";

    /**
     * ID of the client instance making the data request
     */
    private String _clientId;
    /**
     * Node ID
     */
    private String _nodeId;
    /**
     * Total # of reads from this node during the benchmark run
     */
    private Map<String, BenchmarkFragmentStats> _fragments;

    /**
     * Default constructor
     */
    public BenchmarkNodeStats(){};

    public BenchmarkNodeStats(String clientId, String nodeId, Map<String, BenchmarkFragmentStats> fragments){
        _clientId = clientId;
        _nodeId = nodeId;
        _fragments = fragments;
    }

    @JsonGetter(CLIENT_ID_FIELD)
    public String getClientId() {
        return _clientId;
    }
    @JsonSetter(CLIENT_ID_FIELD)
    public void setClientId(String nodeId) {
        this._clientId = nodeId;
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
    public Map<String, BenchmarkFragmentStats> getFragments() {
        return _fragments;
    }
    @JsonSetter(FRAGMENTS_FIELD)
    public void setFragments(Map<String, BenchmarkFragmentStats> reads) {
        this._fragments = reads;
    }

    @JsonIgnore
    public int getTotalNumberOfReads(){
        int res = 0;
        for (String fragKey: this._fragments.keySet()){
            res += this._fragments.get(fragKey).getReadCount();
        }
        return res;
    }
}
