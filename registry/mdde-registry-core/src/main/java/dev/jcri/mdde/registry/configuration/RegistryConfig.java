package dev.jcri.mdde.registry.configuration;

import java.util.List;

/**
 * Config structure for the registry
 * @param <Tstore> storage medium for the registry (not the controlled DB)
 * @param <Tdata> controlled DB nodes configuration
 */
public class RegistryConfig<Tstore, Tdata extends IDataNode> {
    /**
     * Configuration for the registry storage
     */
    private Tstore _registryStore;

    /**
     * Configuration of the controlled database nodes
     */
    private List<Tdata> _dataNodes;

    public Tstore getRegistryStore() {
        return _registryStore;
    }

    public void setRegistryStore(Tstore registryStore) {
        this._registryStore = registryStore;
    }

    public List<Tdata> getDataNodes() {
        return _dataNodes;
    }

    public void setDataNodes(List<Tdata> dataNodes) {
        this._dataNodes = dataNodes;
    }
}
