package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;
import redis.clients.jedis.*;


import java.util.Set;

/**
 * Redis connection helper class for the Redis-backed registry store command handlers.
 * Supports a single node Redis setup, not clusters.
 */
public final class RedisConnectionHelper {
    private RegistryStoreConfigRedis _redisConfig;
    private JedisPool _jedisPool;

    /**
     * Constructor
     * @param configRedis Configuration for the redis instance used
     */
    public RedisConnectionHelper(RegistryStoreConfigRedis configRedis){
        _redisConfig = configRedis;
        String host = _redisConfig.getHost();
        int port = _redisConfig.getPort();

        var configPool = new JedisPoolConfig();
        if (_redisConfig.getPassword() != null) {
            _jedisPool = new JedisPool(configPool, host, port,  2000, _redisConfig.getPassword());
        }
        else{
            _jedisPool = new JedisPool(configPool, host, port,  2000);
        }
    }

    /**
     * Check if connection is possible to the Redis instance
     * @return True if the connection is ok, False otherwise
     */
    public boolean isConnectionOk(){
        try(var jedis = getRedisCommands()){
            var pingResponse = jedis.ping();
            return pingResponse.toLowerCase().equals("pong");
        }
    }

    /**
     * Create a jedis connection object for this config
     * @return jedis instance from the pool that can be used to execute commands
     */
    public Jedis getRedisCommands(){
        if(_jedisPool == null){
            throw new IllegalStateException("Redis connection pool is not initialized");
        }
        return _jedisPool.getResource();
    }

    /**
     * Open a transaction.
     * Remember to make sure to execute Transaction.exec() after declaring all of the statements to it.
     * @param jedis jedis instance
     * @param watchKeys keys that should be watched during the transaction
     * @return Transaction instance for the passed jedis
     */
    public Transaction getTransaction(Jedis jedis, Set<String> watchKeys){
        String[] watchKeysArray = null;
        if(watchKeys.size() > 0) {
            watchKeysArray = new String[watchKeys.size()];
            watchKeys.toArray(watchKeysArray);
        }
        return getTransaction(jedis, watchKeysArray);
    }
    /**
     * Open a transaction.
     * Remember to make sure to execute Transaction.exec() after declaring all of the statements to it.
     * @param jedis jedis instance
     * @param watchKeys keys that should be watched during the transaction
     * @return Transaction instance for the passed jedis
     */
    public Transaction getTransaction(Jedis jedis, String... watchKeys){
        if(watchKeys != null && watchKeys.length > 0) {
            jedis.watch(watchKeys);
        }
        return jedis.multi();
    }
}
