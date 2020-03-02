package dev.jcri.mdde.registry.benchmark.ycsb.stats;

import dev.jcri.mdde.registry.shared.benchmark.ycsb.cli.EYcsbStatsCollector;

import java.io.IOException;
import java.util.Map;

/**
 * Benchmark stats collector factory
 */
public interface IStatsCollectorFactory {
    /**
     * Get the ID of the stats collector client that's passed to the YCSB mdde.* clients as an argument
     * @return EYcsbStatsCollector value (not null)
     */
    EYcsbStatsCollector getCollectorId();

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

    /**
     * If any initial preparation  or staging is required prior executing a benchmark, should done in this function.
     * It's called every time before a benchmark run started.
     */
    void prepare() throws IOException;
}
