package dev.jcri.mdde.registry.benchmark.ycsb;

import dev.jcri.mdde.registry.benchmark.ycsb.out.YCSBOutput;
import dev.jcri.mdde.registry.benchmark.ycsb.out.YCSBOutputParser;

import java.io.*;
import java.util.Objects;

/**
 * Class that runs YCSB and gets the output in the parsed form
 */
public class YCSBRunner implements Closeable {
    /**
     * New line separator specific to the current OS
     */
    private static final String NEWLINE = System.getProperty("line.separator");
    /**
     * Batch executable for YCSB
     */
    private static final String YCSB_WIN = "ycsb.bat";
    /**
     * Bash executable for YCSB
     */
    private static final String YCSB_NIX = "ycsb.sh";

    /**
     * YCSB /bin folder
     */
    private final String _ycsbFolder;
    /**
     * Parser of the YCSB output
     */
    private final YCSBOutputParser _ycsbParser = new YCSBOutputParser();

    /**
     *  Constructor
     * @param ycsbFolder Path to the *folder* where YCSB is located
     */
    public YCSBRunner(String ycsbFolder){
        Objects.requireNonNull(ycsbFolder, "Working folder for YCSB is not specified");
        _ycsbFolder = ycsbFolder;
    }

    /**
     * Load workload data to the YCSB store
     * @param pathToWorkloadFile Path to YCSB workload file
     * @return Parsed YCSB output
     */
    public YCSBOutput loadWorkload(String pathToWorkloadFile,
                                   String pathToMDDENodesConfig,
                                   String ycsbClient) throws IOException {

        // Example ycsb.bat load mdde.redis -P ..\workloads\workloada -p mdde.redis.configfile=.\\git\\YCSB\\mdde.redis\\src\\test\\resources\\test-config.yml
        var command = String.format("%s load %s -P %s -p%s.configfile=%s",
                getYCSBExecutableName(),
                ycsbClient,
                pathToWorkloadFile,
                ycsbClient,
                pathToMDDENodesConfig);

        var strOutput = executeYCSBCommand(_ycsbFolder, command);
        return _ycsbParser.parse(strOutput);
    }

    /**
     * Tun the specified workload
     * @param pathToWorkloadFile Path to YCSB workload file
     * @return Parsed YCSB output
     */
    public YCSBOutput runWorkload(String pathToWorkloadFile,
                                  String pathToMDDENodesConfig,
                                  String ycsbClient) throws IOException {
        // Example ycsb.bat run mdde.redis -P ..\workloads\workloada -p mdde.redis.configfile=.\\git\\YCSB\\mdde.redis\\src\\test\\resources\\test-config.yml
        var command = String.format("%s run %s -P %s -p%s.configfile=%s",
                getYCSBExecutableName(),
                ycsbClient,
                pathToWorkloadFile,
                ycsbClient,
                pathToMDDENodesConfig);

        var strOutput = executeYCSBCommand(_ycsbFolder, command);
        return _ycsbParser.parse(strOutput);
    }

    /**
     * Clear out YCSB data from the database
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        //TODO: Cleanup the nodes
    }

    private String executeYCSBCommand(String pathToYCSBFolder, String command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
        pb.directory(new File(pathToYCSBFolder));
        Process process = pb.start();
        StringBuilder result = new StringBuilder(700);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            while (true)
            {
                String line = in.readLine();
                if (line == null)
                    break;
                result.append(line).append(NEWLINE);
            }
        }
        return result.toString();
    }

    /**
     * Get the name of the YCSB executable that fits the current OS
     * @return Filename of YCSB executable that's expected in the YCSB working folder in the current OS
     */
    private String getYCSBExecutableName(){
        final String currentOsName = System.getProperty("os.name").toLowerCase();
        if(currentOsName.contains("win")){
            return YCSB_WIN;
        }
        return YCSB_NIX;
    }
}
