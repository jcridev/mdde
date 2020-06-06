package dev.jcri.mdde.registry.shared.commands.containers.result.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Container class for Benchmark run results
 */
public class BenchmarkRunResult {
    public static final String THROUGHPUT_FILED = "throughput";
    public static final String NODES_FIELD = "nodes";
    public static final String ERROR_FIELD = "error";
    public static final String INFO_FIELD = "info";

    /**
     * Default constructor
     */
    public BenchmarkRunResult(){}

    /**
     * Overall distribution throughput
     */
    private double _throughput;
    /**
     * Per node statistics resulted from benchmark
     */
    private Collection<BenchmarkNodeStats> _nodes;
    /**
     * If benchmark has failed for any reason, we return an error. Should be null if there was no issues running the
     * benchmark.
     */
    private String _error;

    /**
     * Additional information that might be passed from a benchmark runner. Optional and depends on the runner.
     */
    private Map<String, String> _info = new HashMap<>();

    /**
     * Resulted benchmark run throughput
     * @return Value returned by the benchmark runner
     */
    @JsonGetter(THROUGHPUT_FILED)
    public double getThroughput() {
        return _throughput;
    }
    @JsonSetter(THROUGHPUT_FILED)
    public void setThroughput(double throughput) {
        this._throughput = throughput;
    }

    /**
     * Node-tuple specific statistics
     * @return Per-node statistics
     */
    @JsonGetter(NODES_FIELD)
    public Collection<BenchmarkNodeStats> getNodes() {
        return _nodes;
    }
    @JsonSetter(NODES_FIELD)
    public void setNodes(Collection<BenchmarkNodeStats> nodes) {
        this._nodes = nodes;
    }

    @JsonGetter(ERROR_FIELD)
    public String getError() {
        return _error;
    }
    @JsonSetter(ERROR_FIELD)
    public void setError(String error) {
        this._error = error;
    }

    @JsonGetter(INFO_FIELD)
    public Map<String, String> getInfo(){
        return this._info;
    }
    @JsonSetter(INFO_FIELD)
    public void setInfo(Map<String, String> value){
        this._info = value;
    }
}
