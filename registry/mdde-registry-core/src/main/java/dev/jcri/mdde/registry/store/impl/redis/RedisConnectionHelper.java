package dev.jcri.mdde.registry.store.impl.redis;

import redis.clients.jedis.*;


import java.util.Set;

public final class RedisConnectionHelper {
    /**
     * Lazy singleton
     */
    private static class RedisConnectionHelperLoader {
        private static final RedisConnectionHelper _instance = new RedisConnectionHelper();
    }

    private ConfigRedis _redisConfig;

    private JedisPool _jedisPool;

    private RedisConnectionHelper(){
        if (RedisConnectionHelperLoader._instance != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static RedisConnectionHelper getInstance() {
        return RedisConnectionHelperLoader._instance;
    }

    public void initialize(ConfigRedis configRedis) {
        if(getIsInitialized()){
            throw new IllegalStateException("Redis connection pool is already initialized");
        }
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

    public boolean getIsInitialized(){
        return _jedisPool != null;
    }

    /**
     * Create Jedis connection object for this config
     * @return
     */
    public Jedis getRedisCommands(){
        if(_jedisPool == null){
            throw new IllegalStateException("Redis connection pool is not initialized");
        }
        return _jedisPool.getResource();
    }

    public Transaction getTransaction(Jedis jedis, Set<String> watchKeys){
        String[] watchKeysArray = null;
        if(watchKeys.size() > 0) {
            watchKeysArray = new String[watchKeys.size()];
            watchKeys.toArray(watchKeysArray);
        }
        return getTransaction(jedis, watchKeysArray);
    }

    public Transaction getTransaction(Jedis jedis, String... watchKeys){
        if(watchKeys != null && watchKeys.length > 0) {
            jedis.watch(watchKeys);
        }
        return jedis.multi();
    }
}
