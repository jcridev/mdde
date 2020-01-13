package dev.jcri.mdde.registry.configuration;

import dev.jcri.mdde.registry.configuration.benchmark.YCSBConfig;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.nio.file.Paths;
import java.util.List;

/**
 * Config structure for the registry
 * @param <TStore> storage medium for the registry (not the controlled DB)
 */
@JsonPropertyOrder({
        RegistryConfig.REGISTRY_STORE_FIELD,
        RegistryConfig.DATA_NODES_FIELD,
        RegistryConfig.BENCHMARK_YCSB_FIELD
})
public class RegistryConfig<TStore> {
    public static final String REGISTRY_STORE_FIELD = "store";
    public static final String REGISTRY_TEMP_FOLDER_FIELD = "temp";
    public static final String DATA_NODES_FIELD = "nodes";
    public static final String BENCHMARK_YCSB_FIELD = "bench_ycsb";
     /**
     * Configuration for the registry storage
     */
    private TStore _registryStore;
    /**
     * Configuration of the controlled database nodes
     */
    private List<DBNetworkNodesConfiguration> _dataNodes;
    /**
     * YCSB Benchmark settings
     */
    private YCSBConfig _benchmarkYcsb;
    /**
     * Folder where MDDE-registry creates its temporary files (such as temp YCSB configs)
     */
    private String temp;

    @JsonGetter(REGISTRY_STORE_FIELD)
    public TStore getRegistryStore() {
        return _registryStore;
    }
    @JsonSetter(REGISTRY_STORE_FIELD)
    public void setRegistryStore(TStore registryStore) {
        this._registryStore = registryStore;
    }


    @JsonGetter(DATA_NODES_FIELD)
    public List<DBNetworkNodesConfiguration> getDataNodes() {
        return _dataNodes;
    }
    @JsonSetter(DATA_NODES_FIELD)
    public void setDataNodes(List<DBNetworkNodesConfiguration> dataNodes) {
        this._dataNodes = dataNodes;
    }


    @JsonGetter(BENCHMARK_YCSB_FIELD)
    public YCSBConfig getBenchmarkYcsb() {
        return _benchmarkYcsb;
    }
    @JsonSetter(BENCHMARK_YCSB_FIELD)
    public void setBenchmarkYcsb(YCSBConfig benchmarkYcsb) {
        this._benchmarkYcsb = benchmarkYcsb;
    }

    /**
     * Get path to the MDDE-Registry temporary files folder.
     * If it's null, returns path to {bin_location}/temp
     * @return
     */
    @JsonGetter(REGISTRY_TEMP_FOLDER_FIELD)
    public String getTemp() {
        if(temp != null && !temp.isBlank()){
            return temp;
        }
        return Paths.get(System.getProperty("user.dir"),"temp").toString();
    }
    @JsonSetter(REGISTRY_TEMP_FOLDER_FIELD)
    public void setTemp(String temp) {
        this.temp = temp;
    }
}
