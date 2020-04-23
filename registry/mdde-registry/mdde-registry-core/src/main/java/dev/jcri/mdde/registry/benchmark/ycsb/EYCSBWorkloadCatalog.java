package dev.jcri.mdde.registry.benchmark.ycsb;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Pre-defined YCSB workloads.
 */
public enum EYCSBWorkloadCatalog {
    /**
     * Generate and READ 10000 data records with Zipfian distribution
     */
    READ_10000_ZIPFIAN("read10000zipfian", "/benchmark/ycsb/workload_read_10000_zipfian"),
    /**
     * Generate and READ 10000 data records with 'Latest' distribution
     */
    READ_10000_LATEST("read10000latest", "/benchmark/ycsb/workload_read_10000_latest"),
    /**
     * Generate and READ 10000 data records with Uniform distribution
     */
    READ_10000_UNIFORM("read10000uniform", "/benchmark/ycsb/workload_read_10000_uniform"),
    /**
     * Generate 10000 records and READ 1000 records with 'Latest' distribution
     */
    READ_10000_1000_LATEST("read10000o1000latest", "/benchmark/ycsb/workload_read_10000_1000_latest"),
    /**
     * Generate 100000 records and READ 10000 records with 'Latest' distribution
     */
    READ_100000_1000_LATEST("read100000o1000latest", "/benchmark/ycsb/workload_read_100000_1000_latest"),
    /**
     * Generate 100000 records and READ 1000000 records with 'Latest' distribution
     */
    READ_1000000_1000_LATEST("read1000000o1000latest", "/benchmark/ycsb/workload_read_1000000_1000_latest"),
    /**
     * Generate 100000 records and READ 2000000 records with 'Latest' distribution
     */
    READ_2000000_1000_LATEST("read2000000o1000latest", "/benchmark/ycsb/workload_read_2000000_1000_latest");

    private final String _tag;
    private final String _resourceFile;
    private final String _baseFileName;

    /**
     * Constructor
     * @param tag Unique workload tag used to call it via the registry control API
     * @param resourceFile Path to file in the mdde-registry.core resources
     */
    EYCSBWorkloadCatalog(String tag, String resourceFile){
        if(resourceFile == null || resourceFile.isBlank()){
            throw new IllegalArgumentException("Workload resource name can't be empty");
        }
        _resourceFile = resourceFile;
        _baseFileName= Paths.get(_resourceFile).getFileName().toString();
        if(_baseFileName.isBlank()){
            throw new IllegalArgumentException("Workload resource name is not correct");
        }

        _tag = tag;
    }

    /**
     * File path as defined in the mdde-registry.core resources
     * @return YCSB workload file path relative to  mdde-registry.core resources root
     */
    public String getResourceFileName(){
        return _resourceFile;
    }

    /**
     * File name as defined in the mdde-registry.core resources
     * @return YCSB workload filename (without path)
     */
    public String getResourceBaseFileName(){
        return _baseFileName;
    }

    /**
     * Tag used to invoke the workload via the registry control API
     * @return Custom id Unique within the registry
     */
    public String getTag(){
        return _tag;
    }

    /**
     * File path as defined in the mdde-registry.core resources
     * @return YCSB workload file path relative to  mdde-registry.core resources root
     */
    @Override
    public String toString() {
        return getResourceFileName();
    }

    private static Map<String, EYCSBWorkloadCatalog> _commandsMap = Arrays.stream(EYCSBWorkloadCatalog.values())
            .collect(Collectors.toMap(e -> e._tag, e -> e));

    /**
     * Get a valid YCSB workload by its tag
     * @param tag Tag unique for the workload within MDDE registry
     * @return EYCSBWorkloadCatalog
     * @throws NoSuchElementException Provided YCSB workload tag is not defined within MDDE registry
     */
    public static EYCSBWorkloadCatalog getWorkloadByTag(String tag) throws NoSuchElementException {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        EYCSBWorkloadCatalog command = _commandsMap.get(tag);
        if(command == null){
            throw new NoSuchElementException(tag);
        }
        return command;
    }
}
