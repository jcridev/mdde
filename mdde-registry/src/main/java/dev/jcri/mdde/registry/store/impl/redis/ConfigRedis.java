package dev.jcri.mdde.registry.store.impl.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.commands.BasicCommands;
import redis.clients.jedis.commands.JedisCommands;

import java.beans.Transient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for the redis connection
 */
public class ConfigRedis implements Serializable {
    private String _host;
    private Integer _port;
    private String _username;
    private String _password;
    private Boolean _isCluster;

    /**
     * Config fully detailed constructor
     * @param host Host where the redis node is located
     * @param port Port on which the node is listening
     * @param username Optional username
     * @param password Optional password
     * @param isCluster True - the node is part of a cluster
     */
    public ConfigRedis(String host, int port, String username, String password, boolean isCluster) {
        _host = host;
        _port = port;
        _username = username;
        _password = password;
        _isCluster = isCluster;
    }

    public ConfigRedis(String host, int port, boolean isCluster){
        this(host, port, null, null, isCluster);
    }

    public ConfigRedis(String host, int port){
        this(host, port, null, null, false);
    }

    /**
     * Initialize the default Redis configuration.
     * localhost:6379, non-clustered and no security credentials
     */
    public ConfigRedis(){
        this("localhost", 6379, null, null, false);
    }

    public String getHost() {
        return _host;
    }

    public Integer getPort() {
        return _port;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public Boolean getCluster() {
        return _isCluster;
    }

    /**
     * Create Jedis connection object for this config
     * @return
     */
    @Transient
    public JedisCommands getRedisConnection(){
        String host = getHost();
        int port = getPort();
        JedisCommands jedis = null;
        if (getCluster()) {
            Set<HostAndPort> jedisClusterNodes = new HashSet<>();
            jedisClusterNodes.add(new HostAndPort(host, port));
            jedis = (JedisCommands) new JedisCluster(jedisClusterNodes);
        } else {
            jedis = new Jedis(host, port);
        }
        if (getPassword() != null) {
            ((BasicCommands) jedis).auth(getPassword());
        }
        return jedis;
    }
}
