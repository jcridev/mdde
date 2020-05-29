package dev.jcri.mdde.registry.benchmark.counterfeit;

import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Settings for the 'simulated' benchmark.
 */
public class CounterfeitBenchSettings {
    /**
     * Number of reads associated with the fragment ID.
     */
    private final Map<String, Integer> _readsPerFragment;
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
     * @param readsPerFragment Number of reads associated with the fragment ID.
     * @param baselineThroughput Baseline throughput value.
     */
    private CounterfeitBenchSettings(Map<String, Integer> readsPerFragment,
                                     double baselineThroughput,
                                     String runId){
        _baselineThroughput = baselineThroughput;
        Objects.requireNonNull(readsPerFragment);
        if(readsPerFragment.isEmpty()){
            throw new IllegalArgumentException("Simulated benchmark settings can't be empty");
        }
        // Validate the reads map
        for (Integer value : readsPerFragment.values()) {
            if (value == null ||  value < 0){
                throw new IllegalArgumentException("Read parameters contain invalid values");
            }
        }
        _runId = runId;
        _readsPerFragment = Collections.unmodifiableMap(readsPerFragment);
    }

    /**
     * Settings for the simulated benchmark.
     * @param benchStats Executed benchmark run statistics upon which settings will be formed.
     */
    public CounterfeitBenchSettings(BenchmarkRunResult benchStats, String runId){
        _baselineThroughput = benchStats.getThroughput();

        Map<String, Integer> readCounters = new HashMap<>();
        for (var node : benchStats.getNodes()){
            for (var fragmentId : node.getFragments().keySet()){
                if (!readCounters.containsKey(fragmentId)){
                    readCounters.put(fragmentId, 0);
                }
                var cValue = readCounters.get(fragmentId);
                readCounters.put(fragmentId, cValue + node.getFragments().get(fragmentId).getReadCount());
            }
        }
        _runId = runId;
        _readsPerFragment = Collections.unmodifiableMap(readCounters);
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
}
