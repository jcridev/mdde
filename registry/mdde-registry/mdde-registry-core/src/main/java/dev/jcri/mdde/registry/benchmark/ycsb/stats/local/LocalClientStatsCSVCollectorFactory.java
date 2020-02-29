package dev.jcri.mdde.registry.benchmark.ycsb.stats.local;

import dev.jcri.mdde.registry.benchmark.ycsb.stats.IStatsCollector;
import dev.jcri.mdde.registry.benchmark.ycsb.stats.IStatsCollectorFactory;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.cli.EMddeArgs;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.stats.local.ClientStatsCSVWriter;
import dev.jcri.mdde.registry.store.IReadCommandHandler;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocalClientStatsCSVCollectorFactory implements IStatsCollectorFactory {

    private final String _logsFolder;
    private final IReadCommandHandler _storeReader;

    public LocalClientStatsCSVCollectorFactory(String logsFolder,
                                               IReadCommandHandler storeReader){
        Objects.requireNonNull(logsFolder, "Local file base stats log writer must be provided with a valid path" +
                "to a folder where logs must be stored");
        Objects.requireNonNull(storeReader, "Read-only access to the registry is required");

        _logsFolder = Paths.get(logsFolder).toAbsolutePath().normalize().toString();
        _storeReader = storeReader;
    }

    @Override
    public IStatsCollector getStatsCollector() throws IOException {
        return new LocalClientStatsCSVCollector(_logsFolder, ClientStatsCSVWriter.DEFAULT_DELIMITER, _storeReader);
    }

    @Override
    public Map<String, String> getYCSBParams() {
        Map<String, String> additionalArgs = new HashMap<>();
        additionalArgs.put(EMddeArgs.LOCAL_STATS_DIR_PATH.toString(), _logsFolder);
        return additionalArgs;
    }
}
