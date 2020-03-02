package dev.jcri.mdde.registry.benchmark.ycsb.stats.local;

import dev.jcri.mdde.registry.benchmark.ycsb.stats.IStatsCollector;
import dev.jcri.mdde.registry.benchmark.ycsb.stats.IStatsCollectorFactory;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.cli.EMddeArgs;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.cli.EYcsbStatsCollector;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.stats.local.ClientStatsCSVWriter;
import dev.jcri.mdde.registry.store.IReadCommandHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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
    public EYcsbStatsCollector getCollectorId() {
        return EYcsbStatsCollector.TYPE_STATS_LOCAL_VALUE;
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

    @Override
    public void prepare() throws IOException {
        var logsFolderFile = new File(_logsFolder);
        // Check if the path is a directory
        if (!logsFolderFile.isDirectory()){
            throw new NotDirectoryException(_logsFolder);
        }
        // Create stats folder if it doesn't exist already
        if(!logsFolderFile.exists()){
            logsFolderFile.mkdirs();
            return;
        }
        // Remove any previous run data
        var fNamePattern = String.format("*.{%s,%s}",
                ClientStatsCSVWriter.STATUS_FILE_EXTENSION,
                ClientStatsCSVWriter.LOG_FILE_EXTENSION);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(_logsFolder), fNamePattern)) {
            for (Path file: stream) {
                Files.delete(file);
            }
        }
    }
}
