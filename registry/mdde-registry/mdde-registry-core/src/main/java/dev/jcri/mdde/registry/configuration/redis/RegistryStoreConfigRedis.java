package dev.jcri.mdde.registry.configuration.redis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Configuration for the redis connection used for the registry data store.
 * Connection for a single instance setup.
 */
@JsonPropertyOrder({
        RegistryStoreConfigRedis.REDIS_HOST_FIELD,
        RegistryStoreConfigRedis.REDIS_PORT_FIELD,
        RegistryStoreConfigRedis.REDIS_PASSWORD_FIELD
})
public class RegistryStoreConfigRedis {
    public static final String REDIS_HOST_FIELD = "redis_host";
    public static final String REDIS_PORT_FIELD = "redis_port";
    public static final String REDIS_PASSWORD_FIELD = "redis_password";

    private String _redisHost;
    private int _redisPort;
    private String _password;

    @JsonGetter(REDIS_HOST_FIELD)
    public String getHost() {
        return _redisHost;
    }
    @JsonSetter(REDIS_HOST_FIELD)
    public void setHost(String redisHost) {
        this._redisHost = redisHost;
    }

    @JsonGetter(REDIS_PORT_FIELD)
    public int getPort() {
        return _redisPort;
    }
    @JsonSetter(REDIS_PORT_FIELD)
    public void setPort(int redisPort) {
        this._redisPort = redisPort;
    }

    @JsonGetter(REDIS_PASSWORD_FIELD)
    public String getPassword() {
        return _password;
    }
    @JsonSetter(REDIS_PASSWORD_FIELD)
    public void setPassword(String password) {
        this._password = password;
    }
}
