package dev.jcri.mdde.registry.shared.benchmark.ycsb.stats;

import dev.jcri.mdde.registry.shared.benchmark.ycsb.cli.EMddeArgs;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.cli.EYcsbStatsCollector;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.stats.local.ClientStatsCSVWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * To be used in clients in order to create an appropriate statistics writer
 */
public class ClientStatsWriterFactory {

    /**
     * Constructor
     * @param collector Collector tag
     * @param clientId Unique client ID so that records could be distinguished after the benchmark run
     * @param args All of the arguments passed to YCSB, so that specific collector implementations could retrieve the
     *             values they need
     * @return IClientStatsWriter instance
     * @throws IOException Failure to create a collector due to a collector specific IO error
     */
    public static IClientStatsWriter getStatsWriterInstance(String collector,
                                                            String clientId,
                                                            Map<String, String> args)
            throws IOException {
        EYcsbStatsCollector eVal = EYcsbStatsCollector.get(collector);

        if(eVal == null){
            throw  new IllegalArgumentException(String.format("Unknown stats collector:'%s'",
                    Optional.ofNullable(collector).orElse("")));
        }
        return ClientStatsWriterFactory.getStatsWriterInstance(eVal, clientId, args);
    }

    /**
     * Constructor
     * @param collector Collector tag
     * @param clientId Unique client ID so that records could be distinguished after the benchmark run
     * @param args All of the arguments passed to YCSB, so that specific collector implementations could retrieve the
     *             values they need
     * @return IClientStatsWriter instance
     * @throws IOException Failure to create a collector due to a collector specific IO error
     */
    public static IClientStatsWriter getStatsWriterInstance(EYcsbStatsCollector collector,
                                                            String clientId,
                                                            Map<String, String> args)
            throws IOException {
        Objects.requireNonNull(collector);
        Objects.requireNonNull(clientId, "A valid client ID is required for collecting statistics");

        switch (collector){
            case TYPE_STATS_LOCAL_VALUE:
                return constructLocalCsvWriter(clientId, args);
            default:
                throw new UnsupportedOperationException(
                        String.format("No factory implementation for '%s'", collector.toString()));
        }
    }

    private static IClientStatsWriter constructLocalCsvWriter(String clientId, Map<String, String> args)
            throws IOException {
        String folder = args.get(EMddeArgs.LOCAL_STATS_DIR_PATH.toString());
        if(folder == null){
            throw new IllegalArgumentException(String.format("Stats collector '%s' requires path to the stats collection" +
                    "directory provided in '%s'.",
                    EYcsbStatsCollector.TYPE_STATS_LOCAL_VALUE.toString(), EMddeArgs.LOCAL_STATS_DIR_PATH.toString()));
        }

        return new ClientStatsCSVWriter(folder, clientId);
    }
}
