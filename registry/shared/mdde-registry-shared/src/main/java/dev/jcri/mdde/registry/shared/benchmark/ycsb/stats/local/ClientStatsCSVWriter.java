package dev.jcri.mdde.registry.shared.benchmark.ycsb.stats.local;


import com.opencsv.CSVWriter;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.stats.IClientStatsWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

/**
 * Simple file based statistics collector
 */
public class ClientStatsCSVWriter implements IClientStatsWriter {

    public static final String LOG_FILE_EXTENSION = "csv";
    public static final String STATUS_FILE_EXTENSION = "mdde";
    public static final String STATUS_FILE_FINAL_PREFIX = "done";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    public static final char DEFAULT_DELIMITER = ',';

    private final String _clientId;
    private final char _delimiter;

    private final String _logDir;
    private final DateTimeFormatter _tsFormatter;
    private final Charset _charset;

    private CSVWriter _writer;

    public ClientStatsCSVWriter(String statsDir, String clientId) throws IOException {
        this(statsDir, clientId, DEFAULT_DELIMITER);
    }

    /**
     * Constructor
     * @param statsDir Directory where the client logs should be written
     * @param clientId  Unique ID of the client
     * @param delimiter CSV delimiter
     * @throws IOException
     */
    public ClientStatsCSVWriter(String statsDir,
                                String clientId,
                                Character delimiter) throws IOException {

        Objects.requireNonNull(statsDir, "LocalClientStatsLogCollector requires a folder to look for log");
        Objects.requireNonNull(delimiter, "CSV delimiter is not specified");
        Objects.requireNonNull(clientId, "A client ID, unique within the benchmark run must be provided");
        _delimiter = delimiter;
        _clientId = clientId;
        _charset = StandardCharsets.UTF_8;
        _logDir = Paths.get(statsDir).toAbsolutePath().normalize().toString();
        String _logFile = Paths.get(_logDir, String.format("%s.%s", clientId, LOG_FILE_EXTENSION)).toString();
        Path flagFilePath = Paths.get(_logDir, String.format("%s.%s", clientId, STATUS_FILE_EXTENSION));
        _tsFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);

        // Create the flag file
        java.util.List<String> flagLine = Collections.singletonList(LocalDateTime.now().format(_tsFormatter));
        Files.write(flagFilePath, flagLine,_charset);
        // Open the CSV writer
        _writer = new CSVWriter(new FileWriter(_logFile), _delimiter, '"', '"', "\n");
    }

    /**
     * Add a READ log record
     * @param tupleId Tuple ID that was requested
     * @param nodeId Node ID from which it was read
     * @param success True - successful read
     */
    public void addReadToLog(String tupleId, String nodeId, boolean success){
        this.addReadToLog(tupleId, nodeId, success, LocalDateTime.now());
    }

    /**
     * Add a READ log record.
     * Not thread safe (YCSB is one threaded)
     * @param tupleId Tuple ID that was requested
     * @param nodeId Node ID from which it was read
     * @param success True - successful read
     * @param timestamp Timestamp
     */
    public void addReadToLog(String tupleId, String nodeId, boolean success, LocalDateTime timestamp){
        String[] newLine = new String[5];
        newLine[0] = LogActions.READ.toString();
        newLine[1] = tupleId;
        newLine[2] = nodeId;
        newLine[3] = String.valueOf(success);
        newLine[4] = timestamp.format(_tsFormatter);
        _writer.writeNext(newLine);
    }

    @Override
    public void close() throws IOException {
        IOException writerException = null;
        // Close the writer
        try {
            if (_writer != null) {
                _writer.close();
                _writer = null;
            }
        }
        catch (IOException ex){
            writerException = ex;
        }
        // Make a done flag file
        Path flagFilePath = Paths.get(_logDir,
                String.format("%s.%s.%s", STATUS_FILE_FINAL_PREFIX, _clientId, STATUS_FILE_EXTENSION));
        java.util.List<String> flagLine = Collections.singletonList(LocalDateTime.now().format(_tsFormatter));
        Files.write(flagFilePath, flagLine,_charset);
        // If there was a problem closing the writer, throw
        if(writerException != null){
            throw writerException;
        }
    }
}
