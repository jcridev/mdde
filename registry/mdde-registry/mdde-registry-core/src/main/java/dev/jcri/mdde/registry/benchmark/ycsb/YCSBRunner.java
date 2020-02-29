package dev.jcri.mdde.registry.benchmark.ycsb;

import dev.jcri.mdde.registry.benchmark.ycsb.cli.YCSBOutput;
import dev.jcri.mdde.registry.benchmark.ycsb.cli.YCSBOutputParser;
import dev.jcri.mdde.registry.benchmark.ycsb.stats.IStatsCollector;
import dev.jcri.mdde.registry.benchmark.ycsb.stats.IStatsCollectorFactory;
import dev.jcri.mdde.registry.configuration.benchmark.YCSBConfig;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.MDDEClientConfiguration;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.MDDEClientConfigurationWriter;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.cli.EMddeArgs;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkNodeStats;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.utility.ResourcesTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class that runs YCSB and gets the output in the parsed form
 */
public class YCSBRunner implements Closeable {
    private static final Logger logger = LogManager.getLogger(YCSBRunner.class);
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
    private static final String YCSB_NIX = "sh ycsb.sh";
    /**
     * MDDE client configuration file name
     */
    private static final String TEMP_CLIENT_CONFIG_FILE = "mddeClientConfig." + MDDEClientConfigurationWriter.FILE_EXTENSION;

    /**
     * YCSB /bin folder
     */
    private final YCSBConfig _ycsbConfig;
    /**
     * Temporary subfolder in
     */
    private final Path _tempSubfolder;
    /**
     * Parser of the YCSB output
     */
    private final YCSBOutputParser _ycsbParser = new YCSBOutputParser();

    private final IStatsCollectorFactory _statsCollectorFactory;
    private IStatsCollector _statsCollector;

    private final EYCSBClients _defaultClient;
    /**
     * Path to the *folder* where YCSB is located
     * @param ycsbConfig Configuration for YCSB
     * @param nodes Configuration of the data nodes (database instances)
     * @param connectionProperties Network connectivity settings used by YCSB to interact with the Registry
     */
    public YCSBRunner(YCSBConfig ycsbConfig,
                      List<DBNetworkNodesConfiguration> nodes,
                      Map<String, String> connectionProperties,
                      IStatsCollectorFactory statsCollectorFactory)
    throws IOException{
        Objects.requireNonNull(ycsbConfig.getYcsbBin(), "Working folder for YCSB is not specified");

        if(ycsbConfig.getTemp() == null || ycsbConfig.getTemp().isBlank()){
            throw new IllegalArgumentException("Temporary folder is not specified");
        }

        _tempSubfolder = Paths.get(ycsbConfig.getTemp(),
                                   UUID.randomUUID().toString().replace("-", ""))
                            .toAbsolutePath()
                            .normalize();
        // Create temp folder if needed
        final boolean tempFolderCreated = new File(_tempSubfolder.toString()).mkdirs();
        logger.trace("Created temporary directory {}", tempFolderCreated);

        _ycsbConfig = ycsbConfig;
        _statsCollectorFactory = statsCollectorFactory;

        _defaultClient = EYCSBClients.fromString(ycsbConfig.getYcsbClient());
        if(_defaultClient == null){
            throw new IllegalArgumentException(String.format("Unknown YCSB client %s", ycsbConfig.getYcsbClient()));
        }

        var configWriter = new MDDEClientConfigurationWriter();
        configWriter.writeConfiguration(getYCSBRunConfig(nodes, connectionProperties), getTempClientConfigFilePath());
    }

    /**
     * Get path to the Temporary MDDE client config file
     * @return Path to the temp config file
     */
    private Path getTempClientConfigFilePath(){
        return Paths.get(_tempSubfolder.toString(), TEMP_CLIENT_CONFIG_FILE);
    }

    /**
     * Generate configuration object that should be passed to YCSB
     * @param nodes Configuration of the data nodes (database instances)
     * @param connectionProperties  Network connectivity settings used by YCSB to interact with the Registry
     * @return MDDEClientConfiguration
     */
    private MDDEClientConfiguration getYCSBRunConfig(
            List<DBNetworkNodesConfiguration> nodes,
            Map<String, String> connectionProperties){
        Objects.requireNonNull(connectionProperties, "Network connectivity settings are not specified");
        if(nodes ==null || nodes.size() == 0 ){
            throw new IllegalArgumentException("Data nodes settings are not specified");
        }

        var newConfig = new MDDEClientConfiguration();
        newConfig.setNodes(nodes);
        newConfig.setRegistryNetworkConnection(connectionProperties);

        return newConfig;
    }

    /**
     * Load workload data to the YCSB store
     * @param workload Selected YCSB workload
     * @return Parsed YCSB output
     * @throws IOException
     */
    public YCSBOutput loadWorkload(EYCSBWorkloadCatalog workload) throws IOException {
        Objects.requireNonNull(workload, "Workload id is not supplied");

        var pathToTempWorkload = Paths.get(_tempSubfolder.toString(), workload.getResourceBaseFileName());
        ResourcesTools.copyResourceToFileSystem(workload.getResourceFileName(), pathToTempWorkload);

        return loadWorkload(pathToTempWorkload.toString(),
                getTempClientConfigFilePath().toString(),
                _defaultClient.getClientName());
    }

    /**
     * Load workload data to the YCSB store
     * @param pathToWorkloadFile Path to YCSB workload file
     * @return Parsed YCSB output
     */
    private YCSBOutput loadWorkload(String pathToWorkloadFile,
                                   String pathToMDDENodesConfig,
                                   String ycsbClient) throws IOException {

        if(ycsbClient == null || ycsbClient.isBlank()){
            throw new IllegalArgumentException("Specific YCSB client must be set");
        }

        // Example ycsb.bat load mdde.redis -P ..\workloads\workloada -p mdde.redis.configfile=.\\test-config.yml
        var command = String.format("%s load %s -P %s -p %s=%s",
                getYCSBExecutableName(),
                ycsbClient,
                pathToWorkloadFile,
                EMddeArgs.CONFIG_FILE,
                pathToMDDENodesConfig);

        var strOutput = executeYCSBCommand(_ycsbConfig.getYcsbBin(), command);
        return _ycsbParser.parse(strOutput);
    }

    /**
     * Run the specified workload
     * @param workload Selected YCSB workload
     * @return
     * @throws IOException
     */
    public YCSBOutput runWorkload(EYCSBWorkloadCatalog workload) throws IOException {
        var pathToTempWorkload = Paths.get(_tempSubfolder.toString(), workload.getResourceBaseFileName());
        if(!Files.exists(pathToTempWorkload)) {
            ResourcesTools.copyResourceToFileSystem(workload.getResourceFileName(), pathToTempWorkload);
        }

        return runWorkload(pathToTempWorkload.toString(),
                getTempClientConfigFilePath().toString(),
                _defaultClient.getClientName(), _ycsbConfig.getYcsbWorkerThreads());
    }

    /**
     * Run the specified workload
     * @param pathToWorkloadFile Path to YCSB workload file
     * @return Parsed YCSB output
     */
    private YCSBOutput runWorkload(String pathToWorkloadFile,
                                  String pathToMDDENodesConfig,
                                  String ycsbClient,
                                  int threads) throws IOException {
        // Example ycsb.bat run mdde.redis -P ..\workloads\workloada -threads 10 -p mdde.redis.configfile=.\\test-config.yml
        var command = String.format("%s run %s -P %s -threads %d -p %s=%s",
                getYCSBExecutableName(),
                ycsbClient,
                pathToWorkloadFile,
                threads,
                EMddeArgs.CONFIG_FILE,
                pathToMDDENodesConfig);

        if(_statsCollectorFactory != null){
            var additionalParams = _statsCollectorFactory.getYCSBParams();
            if(additionalParams != null && additionalParams.size() > 0){
                StringBuilder sb = new StringBuilder();
                for(var entry: additionalParams.entrySet()){
                    var value=entry.getValue();
                    if(value != null){
                        var argument=entry.getKey();
                        sb.append(String.format(" -p %s=%s",argument, value ));
                    }
                }
                command += sb.toString();
            }
        }

        var strOutput = executeYCSBCommand(_ycsbConfig.getYcsbBin(), command);
        if(_statsCollectorFactory != null) {
            _statsCollector = _statsCollectorFactory.getStatsCollector();
        }
        return _ycsbParser.parse(strOutput);
    }

    /**
     * Check if the stats are ready for collection
     * @return True - stats are ready; False - not ready; null - no stats.
     */
    public Boolean statsResultsReady() throws IOException {
        if(_statsCollector == null){
            return null;
        }
        return _statsCollector.getStatsReady();
    }

    /**
     * Retrieve collected stats for the run
     * @return
     * @throws IOException
     * @throws MddeRegistryException
     */
    public Collection<BenchmarkNodeStats> getStats() throws IOException, MddeRegistryException {
        if(!statsResultsReady()){
            throw new IllegalStateException("No stats ready for collection");
        }
        return _statsCollector.getFragmentStats();
    }

    /**
     * Cleanup
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        // Remove temp folder
        try {
            if (Files.exists(_tempSubfolder)) {
                Files.delete(_tempSubfolder);
            }
        }
        catch (Exception e){
            logger.error("Unable to delete temporary folder {}", _tempSubfolder, e);
        }
        try{
            if(_statsCollector != null){
                _statsCollector.close();
            }
        } catch (Exception e){
            logger.error("Unable to clean out stats after the run", e);
        }
    }

    private String executeYCSBCommand(String pathToYCSBFolder, String command) throws IOException {
        var pbArgs = command.split(" ");
        ProcessBuilder pb = new ProcessBuilder(pbArgs).redirectErrorStream(true);
        var ycsbAbsolutePath = Paths.get(pathToYCSBFolder).normalize().toAbsolutePath().toString();
        logger.trace("Looking fot YCSB bin in {}", ycsbAbsolutePath);
        pb.directory(new File(ycsbAbsolutePath));
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
