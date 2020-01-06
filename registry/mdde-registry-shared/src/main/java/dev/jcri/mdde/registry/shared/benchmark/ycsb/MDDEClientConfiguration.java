package dev.jcri.mdde.registry.shared.benchmark.ycsb;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.util.List;

/**
 * Generic configuration for MDDE access through network
 */
public class MDDEClientConfiguration {
    private List<DBNetworkNodesConfiguration> redisMDDEClientNodes;
    private String mddeRegistryHost;
    private int mddeRegistryPort;

    /**
     * Get Redis Instances.
     * @return Redis data nodes configurations.
     */
    @JsonGetter("nodes")
    public List<DBNetworkNodesConfiguration> getNodes() {
        return redisMDDEClientNodes;
    }

    /**
     * Set Redis Instances.
     * @param redisNodes List of the known Nodes configurations.
     */
    @JsonSetter("nodes")
    public void setNodes(List<DBNetworkNodesConfiguration> redisNodes) {
        redisMDDEClientNodes = redisNodes;
    }

    /**
     * Get host where MDDE registry is running.
     * @return domain / ip
     */
    @JsonGetter("mddeHost")
    public String getMddeRegistryHost() {
        return mddeRegistryHost;
    }

    /**
     * Set host where MDDE registry is running.
     * @param host domain / ip
     */
    @JsonSetter("mddeHost")
    public void setMddeRegistryHost(String host) {
        this.mddeRegistryHost = host;
    }

    /**
     * Get port where MDDE registry is listening on the specified host.
     * @return Port number.
     */
    @JsonGetter("mddePort")
    public int getMddeRegistryPort() {
        return mddeRegistryPort;
    }

    /**
     * Set port where MDDE registry is listening on the specified host.
     * @param port Port number.
     */
    @JsonSetter("mddePort")
    public void setMddeRegistryPort(int port) {
        this.mddeRegistryPort = port;
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
