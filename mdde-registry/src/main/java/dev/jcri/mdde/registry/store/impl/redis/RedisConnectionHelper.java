package dev.jcri.mdde.registry.store.impl.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.commands.BasicCommands;
import redis.clients.jedis.commands.JedisCommands;


import java.util.Set;

public class RedisConnectionHelper {
    private final ConfigRedis _redisConfig;

    private JedisCommands _jedisCommands;

    public RedisConnectionHelper(ConfigRedis configRedis){
        _redisConfig = configRedis;
    }

    private void initializeConnection(){
        String host = _redisConfig.getHost();
        int port = _redisConfig.getPort();
        Jedis jedis = null;
        jedis = new Jedis(host, port);
        if (_redisConfig.getPassword() != null) {
            ((BasicCommands) jedis).auth(_redisConfig.getPassword());
        }
        _jedisCommands = jedis;
        jedis.connect();
    }

    /**
     * Create Jedis connection object for this config
     * @return
     */
    public JedisCommands getRedisCommands(){
        if(_jedisCommands == null){
            initializeConnection();
        }
        return _jedisCommands;
    }

    public Transaction getTransaction(){
        return getTransaction((String[]) null);
    }

    public Transaction getTransaction(Set<String> watchKeys){
        String[] watchKeysArray = null;
        if(watchKeys.size() > 0) {
            watchKeysArray = new String[watchKeys.size()];
            watchKeys.toArray(watchKeysArray);
        }
        return getTransaction(watchKeysArray);
    }

    public Transaction getTransaction(String[] watchKeys){
        Jedis jedis = (Jedis) _jedisCommands;
        if(watchKeys != null && watchKeys.length > 0) {
            jedis.watch(watchKeys);
        }
        return jedis.multi();
    }

    public Pipeline getPipeline(){
        return ((Jedis) _jedisCommands).pipelined();
    }
}
