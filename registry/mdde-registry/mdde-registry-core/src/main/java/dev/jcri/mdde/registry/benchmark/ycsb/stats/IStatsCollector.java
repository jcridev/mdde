package dev.jcri.mdde.registry.benchmark.ycsb.stats;

import dev.jcri.mdde.registry.benchmark.ycsb.stats.result.NodeBenchmarkStats;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public interface IStatsCollector extends Closeable {
    /**
     * Return true if the statistics for the current benchmark run is ready for collection. After this method returns
     * True, getStats() will be invoked
     * @return True - benchmark run statistics is ready for collection
     */
    boolean getStatsReady() throws IOException;

    /**
     * Return statistics for benchmark run relevant to the fragments specifically
     * @return Fragment level statistics
     */
    Collection<NodeBenchmarkStats> getFragmentStats() throws IOException, MddeRegistryException;
}
