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
    public static final String DATA_NODES_FIELD = "nodes";
    public static final String BENCHMARK_YCSB_FIELD = "bench_ycsb";
    public static final String SNAPSHOTS_FOLDER_FIELD = "snapshot_dir";
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
     * Directory where the registry and data node snapshots should be placed
     */
    private String _snapshotsDir;

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

    @JsonGetter(SNAPSHOTS_FOLDER_FIELD)
    public String getSnapshotsDir() {
        return _snapshotsDir;
    }
    @JsonSetter(SNAPSHOTS_FOLDER_FIELD)
    public void setSnapshotsDir(String snapshotsDir) {
        this._snapshotsDir = snapshotsDir;
    }
}
