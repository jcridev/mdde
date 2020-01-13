package dev.jcri.mdde.registry.shared.configuration;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Configuration for Clients connecting to the MDDE registry through network
 */
public class MDDERegistryNetworkConfiguration {
    public final static String MDDE_REGISTRY_HOST_FIELD = "mddeHost";
    public final static String MDDE_REGISTRY_CONTROL_PORT_FIELD = "mddePort";
    public final static String MDDE_REGISTRY_BENCHMARK_PORT_FIELD = "mddeBenchPort";


    private String mddeRegistryHost;
    private int mddeRegistryPort;
    private Integer mddeRegistryBenchmarkPort;

    /**
     * Get host where MDDE registry is running.
     * @return domain / ip
     */
    @JsonGetter(MDDE_REGISTRY_HOST_FIELD)
    public String getMddeRegistryHost() {
        return mddeRegistryHost;
    }

    /**
     * Set host where MDDE registry is running.
     * @param host domain / ip
     */
    @JsonSetter(MDDE_REGISTRY_HOST_FIELD)
    public void setMddeRegistryHost(String host) {
        this.mddeRegistryHost = host;
    }

    /**
     * Get port where MDDE registry is listening on the specified host.
     * @return Port number.
     */
    @JsonGetter(MDDE_REGISTRY_CONTROL_PORT_FIELD)
    public int getMddeRegistryPort() {
        return mddeRegistryPort;
    }

    /**
     * Set port where MDDE registry is listening on the specified host.
     * @param port Port number.
     */
    @JsonSetter(MDDE_REGISTRY_CONTROL_PORT_FIELD)
    public void setMddeRegistryPort(int port) {
        this.mddeRegistryPort = port;
    }

    /**
     * Set a separate port for Benchmark operations is the Network interface utilized multiple ports
     * @return null if benchmark port is not set
     */
    @JsonGetter(MDDE_REGISTRY_BENCHMARK_PORT_FIELD)
    public Integer getMddeRegistryBenchmarkPort() {
        return mddeRegistryBenchmarkPort;
    }

    /**
     * Get a separate port for Benchmark operations is the Network interface utilized multiple ports
     * @param mddeRegistryBenchmarkPort Integer value in a valid network ports range
     */
    @JsonSetter(MDDE_REGISTRY_BENCHMARK_PORT_FIELD)
    public void setMddeRegistryBenchmarkPort(Integer mddeRegistryBenchmarkPort) {
        this.mddeRegistryBenchmarkPort = mddeRegistryBenchmarkPort;
    }
}
