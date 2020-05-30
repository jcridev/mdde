package dev.jcri.mdde.registry.benchmark.counterfeit;

import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.utility.MutableCounter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Settings for the 'simulated' benchmark.
 */
public class CounterfeitBenchSettings {
    /**
     * Number of reads associated with the fragment ID.
     */
    private final Map<String, Integer> _readsPerFragment;

    /**
     * Percentage of of reads served by each node.
     */
    private final Map<String, Double> _nodeReadBalance;

    /**
     * Baseline throughput value.
     */
    private final double _baselineThroughput;
    /**
     * If these settings are based on the benchmark run, save this as property.
     */
    private final String _runId;

    /**
     * Settings for the simulated benchmark.
     * @param benchStats Executed benchmark run statistics upon which settings will be formed.
     */
    public CounterfeitBenchSettings(BenchmarkRunResult benchStats, String runId){
        _baselineThroughput = benchStats.getThroughput();
        int totalReads = 0;
        // Total reads per node
        Map<String, MutableCounter> readNodeCounters = new HashMap<>();
        // Total reads per fragment
        Map<String, Integer> readFragCounters = new HashMap<>();
        for (var node : benchStats.getNodes()){
            // Sum up total reads
            totalReads += node.getTotalNumberOfReads();
            // Sum up total reads per node
            var nodeCounter = readNodeCounters.get(node.getNodeId());
            if(nodeCounter == null){
                readNodeCounters.put(node.getNodeId(), new MutableCounter(node.getTotalNumberOfReads()));
            }
            else{
                nodeCounter.add(node.getTotalNumberOfReads());
            }
            // Sum up total reads per fragment
            for (var fragmentId : node.getFragments().keySet()){
                if (!readFragCounters.containsKey(fragmentId)){
                    readFragCounters.put(fragmentId, 0);
                }
                var cValue = readFragCounters.get(fragmentId);
                readFragCounters.put(fragmentId, cValue + node.getFragments().get(fragmentId).getReadCount());
            }
        }
        _runId = runId;
        _readsPerFragment = Collections.unmodifiableMap(readFragCounters);

        // Get participation percentage
        Map<String, Double> nodeParticipation = new HashMap<>();
        for (var rNodeCntId: readNodeCounters.keySet()){
            var cNCounter = readNodeCounters.get(rNodeCntId).get();
            nodeParticipation.put(rNodeCntId, (double)cNCounter / totalReads);
        }
        _nodeReadBalance = nodeParticipation;
    }

    /**
     * Simulated reads.
     * @return Read-only map of fragment IDs to the desired number of reads per simulated benchmark run.
     */
    public Map<String, Integer> getReads() {
        return _readsPerFragment;
    }

    /**
     * Get baseline throughput value.
     * @return Baseline throughput value.
     */
    public double getBaselineThroughput() {
        return _baselineThroughput;
    }

    /**
     * ID of the benchmark run upon which these settings are based.
     * @return Benchmark run ID.
     */
    public String getRunId() {
        return _runId;
    }

    /**
     * Get total number of reads
     * @return Total number of reads to perform.
     */
    public int getTotalReads(){
        int result = 0;
        for (var fragId: _readsPerFragment.keySet()){
            result += _readsPerFragment.get(fragId);
        }
        return result;
    }

    /**
     * Percentage of of reads served by each node.
     * @return Percentage of of reads served by each node.
     */
    public Map<String, Double> getNodeReadBalance() {
        return _nodeReadBalance;
    }
}
