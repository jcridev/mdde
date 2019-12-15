package dev.jcri.mdde.registry.store.impl.redis;

import java.io.Serializable;

/**
 * Configuration for the redis connection
 */
public class ConfigRedis implements Serializable {
    private String _host;
    private Integer _port;
    private String _password;

    /**
     * Config fully detailed constructor
     * @param host Host where the redis node is located
     * @param port Port on which the node is listening
     * @param password Optional password
     */
    public ConfigRedis(String host, int port, String password) {
        _host = host;
        _port = port;
        _password = password;
    }

    public ConfigRedis(String host, int port){
        this(host, port, null);
    }

    /**
     * Initialize the default Redis configuration.
     * localhost:6379, non-clustered and no security credentials
     */
    public ConfigRedis(){
        this("localhost", 6379, null);
    }

    public String getHost() {
        return _host;
    }

    public Integer getPort() {
        return _port;
    }

    public String getPassword() {
        return _password;
    }
}
