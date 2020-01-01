package dev.jcri.mdde.registry.configuration.redis;

/**
 * Configuration for the redis connection used for the registry data store.
 * Connection for a single instance setup.
 */
public class RegistryStoreConfigRedis {
    private String _redisHost;
    private int _redisPort;
    private String _password;

    public String getHost() {
        return _redisHost;
    }

    public void setHost(String redisHost) {
        this._redisHost = redisHost;
    }

    public int getPort() {
        return _redisPort;
    }

    public void setPort(int redisPort) {
        this._redisPort = redisPort;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }
}
