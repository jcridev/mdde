package dev.jcri.mdde.registry.shared.benchmark.ycsb.stats;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;

public class ClientStatsCSVReader {

    private final String _logFile;

    public ClientStatsCSVReader(String logFile){
        Objects.requireNonNull(logFile, "logFile can't be null");

        _logFile = logFile;
    }

    public CSVReader reader() throws FileNotFoundException {
        return new CSVReader(new FileReader(_logFile));
    }
}

