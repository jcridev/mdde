package dev.jcri.mdde.registry.data.impl.redis;

import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.data.ShuffleKeysResult;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data shuffle for Redis non-clustered data nodes
 */
public class RedisDataShuffler implements IDataShuffler {
    /**
     * NodeId : JedisPool
     */
    private final Map<String, JedisPool> _redisConnections;

    /**
     * Constructor
     * @param redisNodes List of the Redis instances managed by this registry
     */
    public RedisDataShuffler(List<DBNetworkNodesConfiguration> redisNodes){
        Objects.requireNonNull(redisNodes, "Redis nodes list is null");
        if(redisNodes.size() == 0){
            throw new IllegalArgumentException("List of Redis nodes is empty");
        }

        // Fill out connections pools for the registry data nodes
        _redisConnections = new HashMap<>();
        for(var dataNode: redisNodes){
            var configPool = new JedisPoolConfig();
            if (dataNode.getPassword() != null) {
                _redisConnections.put(dataNode.getNodeId(), new JedisPool(configPool,
                                                                dataNode.getHost(),
                                                                dataNode.getPort(),
                                                                2000,
                                                                new String(dataNode.getPassword())));
            }
            else{
                _redisConnections.put(dataNode.getNodeId(), new JedisPool(configPool,
                                                            dataNode.getHost(),
                                                            dataNode.getPort(),
                                                            2000));
            }
        }
    }


    @Override
    public ShuffleKeysResult copyTuples(String sourceNodeId, String destinationNodeId, Set<String> tupleIds) {
        if(tupleIds == null || tupleIds.size() == 0){
            throw new IllegalArgumentException("No tuple IDs for copy are specified");
        }
        JedisPool sourcePool = _redisConnections.get(sourceNodeId);
        if(sourcePool == null){
            throw new IllegalArgumentException(
                    String.format("Unable to find source data node with id: %s", sourceNodeId));
        }
        JedisPool destinationPool = _redisConnections.get(destinationNodeId);
        if(destinationPool == null){
            throw new IllegalArgumentException(
                    String.format("Unable to find destination data node with id: %s", destinationNodeId));
        }

        // Get types of the keys
        Map<String, Response<String>> keyTypes = new HashMap<>();
        try(Jedis jedis = sourcePool.getResource()){
            try(Pipeline p = jedis.pipelined()) {
                for (var key : tupleIds) {
                    keyTypes.put(key, p.type(key));
                }
            }
        }

        // Failed to retrieve key types
        Set<String> nullKeys = keyTypes.entrySet()
                .stream()
                .filter(entry -> getRedisKeyType(entry.getValue().get()) == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        // None of the keys were retrieved
        if(nullKeys.size() == tupleIds.size()){
            return new ShuffleKeysResult(null,
                                        nullKeys,
                                        new IOException("Unable to retrieve key types."));
        }

        Set<String> processedKeys = new HashSet<>();
        // Process STRING keys
        var stringKeys = getKeysOfType(keyTypes, redisKeyTypes.string);
        if(stringKeys.size() > 0){
            processedKeys.addAll(stringKeys);
            var stringKeysArray = stringKeys.toArray(new String[0]);
            List<String> results;
            try(Jedis jedis = sourcePool.getResource()){
                results = jedis.mget(stringKeysArray);
            }
            String[] keyValues = new String[stringKeysArray.length * 2];
            int keyValuesWriteIdx = 0;
            for (int i = 0; i < stringKeysArray.length; i++) {
                String key = stringKeysArray[i];
                String value = results.get(i);
                keyValues[keyValuesWriteIdx ++] = key;
                keyValues[keyValuesWriteIdx ++] = value;
            }
            try(Jedis jedis = destinationPool.getResource()){
                var stringCopied = jedis.mset(keyValues);
            }
        }

        // Process SET keys
        var setKeys = getKeysOfType(keyTypes, redisKeyTypes.set);
        if(setKeys.size() > 0){
            processedKeys.addAll(setKeys);
            // Done sequentially because Redis SET size can be arbitrary, so we don't want to risk running out of RAM
            for(var setKey: setKeys){
                Set<String> sourceSetMembers;
                try(Jedis jedis = sourcePool.getResource()){
                    sourceSetMembers = jedis.smembers(setKey);
                }
                var sourceSetMembersArray = sourceSetMembers.toArray(new String[0]);
                long addedMembers = 0;
                try(Jedis jedis = destinationPool.getResource()){
                    addedMembers = jedis.sadd(setKey, sourceSetMembersArray);
                }
            }
        }

        // Process LIST keys
        var listKeys = getKeysOfType(keyTypes, redisKeyTypes.list);
        if(listKeys.size() > 0){
            processedKeys.addAll(listKeys);
            for(var listKey: listKeys){
                List<String> listItems;
                try(Jedis jedis = sourcePool.getResource()){
                    listItems = jedis.lrange(listKey, 0, -1);
                }
                long addedMembers = 0;
                try(Jedis jedis = destinationPool.getResource()){
                    addedMembers = jedis.rpush(listKey, listItems.toArray(new String[0]));
                }
            }
        }

        // Process HASH keys
        var hashKeys = getKeysOfType(keyTypes, redisKeyTypes.hash);
        if(hashKeys.size() > 0){
            processedKeys.addAll(hashKeys);
            for(var hashKey: hashKeys){
                Map<String, String> hashValues;
                try(Jedis jedis = sourcePool.getResource()){
                    hashValues = jedis.hgetAll(hashKey);
                }
                long addedMembers = 0;
                try(Jedis jedis = destinationPool.getResource()){
                    addedMembers = jedis.hset(hashKey, hashValues);
                }
            }
        }
        return new ShuffleKeysResult(processedKeys, nullKeys, null);
    }

    @Override
    public ShuffleKeysResult deleteTuples(String nodeId, Set<String> tupleIds){
        if(tupleIds == null || tupleIds.size() == 0){
            throw new IllegalArgumentException("No tuple IDs for copy are specified");
        }
        JedisPool connPool = _redisConnections.get(nodeId);
        if(connPool == null){
            throw new IllegalArgumentException(
                    String.format("Unable to find data node with id: %s", nodeId));
        }

        Map<String, Response<Long>> keyResults = new HashMap<>();
        try(Jedis jedis = connPool.getResource()){
            try(Pipeline p = jedis.pipelined()) {
                for (var key : tupleIds) {
                    keyResults.put(key, p.del(key));
                }
                p.sync();
            }
        }

        Set<String> missedKeys = keyResults.entrySet()
                .stream()
                .filter(entry -> entry.getValue().get() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if(missedKeys.size() == 0){
            return new ShuffleKeysResult(tupleIds);
        }
        else{
            if(missedKeys.size() != tupleIds.size()){
                var tmpProcessed = new HashSet<>(tupleIds);
                tmpProcessed.removeAll(missedKeys);
                return new ShuffleKeysResult(tmpProcessed, missedKeys, null);
            }
            else{
                return new ShuffleKeysResult(null, missedKeys, null);
            }
        }
    }

    @Override
    public boolean flushData() {
        if(_redisConnections == null || _redisConnections.size() == 0){
            return false; // No connections, nothing to remove
        }

        for(var connectionPool: _redisConnections.values()){
            try(Jedis jedis = connectionPool.getResource()){
                jedis.flushAll();
            }
        }
        return true;
    }

    /**
     * Get type of the key
     * @param connection Jedis connection instance
     * @param key Key to look for
     * @return redisKeyTypes value or null if the type of the key is unknown
     */
    private redisKeyTypes getRedisKeyType(Jedis connection, String key){
        var keyType = connection.type(key);
        if(keyType == null){
            return null;
        }
        return getRedisKeyType(keyType);
    }

    /**
     * Get type type of the key from the type name returned by Redis
     * @param redisTypeString Result of TYPE
     * @return redisKeyTypes value or null if the type of the key is unknown
     */
    private redisKeyTypes getRedisKeyType(String redisTypeString){
        switch (redisTypeString.toLowerCase()){
            case "string":
                return redisKeyTypes.string;
            case "list":
                return redisKeyTypes.list;
            case "set":
                return redisKeyTypes.set;
            case "hash":
                return redisKeyTypes.hash;
            default:
                return null;
        }
    }

    /**
     * Get all of the keys of a specific type the results of a pipelined type retrieval call
     * @param keyTypes Key : Redis TYPE response
     * @param type Desired type
     * @return Set of the keys of the desired type
     */
    private Set<String> getKeysOfType(Map<String, Response<String>> keyTypes, redisKeyTypes type){
         return keyTypes.entrySet()
                         .stream()
                         .filter(entry -> getRedisKeyType(entry.getValue().get()) == type)
                         .map(Map.Entry::getKey)
                         .collect(Collectors.toSet());
    }

    /**
     * Types of the redis keys
     */
    private enum redisKeyTypes{
        string,
        list,
        set,
        hash
    }
}
