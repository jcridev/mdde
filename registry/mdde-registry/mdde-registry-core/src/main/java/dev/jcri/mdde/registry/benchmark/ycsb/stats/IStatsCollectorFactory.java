package dev.jcri.mdde.registry.benchmark.ycsb.stats;

import java.io.IOException;
import java.util.Map;

/**
 * Benchmark stats collector factory
 */
public interface IStatsCollectorFactory {
    /**
     * Stats collector instance created before running the benchmark
     * @return IStatsCollector instance
     */
    IStatsCollector getStatsCollector() throws IOException;

    /**
     * When YCSB requires additional parametrization to interface with the selected stats collector, return them as
     * key value pairs, where key is a CLI argument that will be passed to YCSB when it's invoked, and the value
     * @return Dictionary <Custom YCSB CLI param, String value>
     */
    Map<String, String> getYCSBParams();
}
