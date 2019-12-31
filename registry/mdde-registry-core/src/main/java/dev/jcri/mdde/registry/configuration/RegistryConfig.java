package dev.jcri.mdde.registry.configuration;

import java.net.InetAddress;

public class RegistryConfig {
    /**
     * Interface (IP address) which the server will be listening to.
     * Default value is null, meaning listening to all addresses.
     */
    private InetAddress _listenInterface = null;
    /**
     * Port on which the server is running
     */
    private int _listenPort=8942;

    /**
     * Configuration for the registry storage
     */
    private RegistryDataStoreConfig _backStoreConfig;
}
