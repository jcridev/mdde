package dev.jcri.mdde.registry.configuration;

/**
 *
 */
public class RegistryDataStoreConfig {
    private String _redisHost;
    private int _redisPort;

    public String getRedisHost() {
        return _redisHost;
    }

    public void setRedisHost(String redisHost) {
        this._redisHost = redisHost;
    }

    public int getRedisPort() {
        return _redisPort;
    }

    public void setRedisPort(int redisPort) {
        this._redisPort = redisPort;
    }
}
