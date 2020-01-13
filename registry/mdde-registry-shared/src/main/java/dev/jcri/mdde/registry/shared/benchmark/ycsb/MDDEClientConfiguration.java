package dev.jcri.mdde.registry.shared.benchmark.ycsb;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.shared.configuration.MDDERegistryNetworkConfiguration;

import java.io.IOException;
import java.util.List;

/**
 * Generic configuration for MDDE access through network
 */
public class MDDEClientConfiguration {
    public final static String NODES_FIELD = "nodes";
    public final static String REGISTRY_NETWORK_FIELD = "mddePort";

    private List<DBNetworkNodesConfiguration> MDDEClientNetworkNodes;
    private MDDERegistryNetworkConfiguration registryNetworkConnection;

    /**
     * Get Redis Instances.
     * @return Redis data nodes configurations.
     */
    @JsonGetter(NODES_FIELD)
    public List<DBNetworkNodesConfiguration> getNodes() {
        return MDDEClientNetworkNodes;
    }
    /**
     * Set Redis Instances.
     * @param redisNodes List of the known Nodes configurations.
     */
    @JsonSetter(NODES_FIELD)
    public void setNodes(List<DBNetworkNodesConfiguration> redisNodes) {
        MDDEClientNetworkNodes = redisNodes;
    }

    /**
     * Get settings for connecting to MDDE Registry through network
     * @return null if the network connection interface is not specified for the registry connectiojn
     */
    @JsonGetter(REGISTRY_NETWORK_FIELD)
    public MDDERegistryNetworkConfiguration getRegistryNetworkConnection() {
        return registryNetworkConnection;
    }

    /**
     * Set settings for connecting to MDDE Registry through network
     * @param registryNetworkConnection Host / Port parameters
     */
    @JsonSetter(REGISTRY_NETWORK_FIELD)
    public void setRegistryNetworkConnection(MDDERegistryNetworkConfiguration registryNetworkConnection) {
        this.registryNetworkConnection = registryNetworkConnection;
    }

    @Override
    public String toString() {
        YAMLMapper mapper = new YAMLMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            return null;
        }
    }
}
