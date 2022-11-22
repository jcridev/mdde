package dev.jcri.mdde.registry.data.impl.redis;

import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.data.ShuffleKeysResult;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.store.queue.actions.DataAction;
import dev.jcri.mdde.registry.utility.ByteTools;
import redis.clients.jedis.*;
import redis.clients.jedis.resps.ScanResult;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
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

    /**
     * Copy (replicate) a set of tuples from one node to another
     * @param sourceNodeId ID of the data node from where the Tuples should be copied
     * @param destinationNodeId ID of the data node where the copy of the Tuples should be paced
     * @param tupleIds IDs of the tuples that are located
     * @return
     */
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
                p.sync();
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

    @Override
    public boolean dumpToFile(String pathToFile, boolean overwrite) throws IOException {
        File dumpFile = new File(pathToFile);
        if(dumpFile.exists()) {
            if(dumpFile.isDirectory()){
                throw new IOException(String.format("Specified path is a directory: '%s'", pathToFile));
            }
            if(!overwrite){
                throw new FileAlreadyExistsException(String.format("Specified file already exists: '%s'", pathToFile));
            }
            else{
                if(!dumpFile.delete()){
                    throw new IOException(String.format("Unable to overwrite the existing file: '%s'", pathToFile));
                }
            }
        }
        final Charset serializationCharset = StandardCharsets.UTF_8;
        try(FileOutputStream out = new FileOutputStream(dumpFile)){
            try(FileLock outLock = out.getChannel().lock()){
                // Write version of the file
                // 2 bytes
                short version = 0;
                out.write(ByteTools.shortToByteArray(version));
                // Write number and ids of the nodes
                // 4 bytes
                var nodeIds = _redisConnections.keySet();
                out.write(ByteTools.intToByteArray(nodeIds.size()));
                // Write keys per node
                for(var nodeId: nodeIds){
                    // |node id len|node id val|
                    // |2 bytes    |...
                    byte[] nodeIdBytes = nodeId.getBytes(serializationCharset);
                    out.write(ByteTools.shortToByteArray((short) nodeIdBytes.length));
                    out.write(nodeIdBytes);
                    // Write keys and values
                    try(Jedis jedis = _redisConnections.get(nodeId).getResource()){
                        // |number of keys in the node|
                        // 8 bytes
                        var nKeys = jedis.dbSize();
                        out.write(ByteTools.longToByteArray(nKeys));

                        String scanCursor = "0";
                        while(true){
                            // Read one by one, slower but easier on RAM
                            ScanResult<String> scan = jedis.scan(scanCursor);
                            scanCursor = scan.getCursor();
                            if(scan.getResult() != null && scan.getResult().size() > 0){
                                // Dump keys
                                for(String key: scan.getResult()){
                                    byte[] value = jedis.dump(key);
                                    // |key len|key val|value len|value val|
                                    // |4 bytes|...    |4 bytes  |...      |
                                    // Key
                                    var keyBytes = key.getBytes(serializationCharset);
                                    out.write(ByteTools.intToByteArray(keyBytes.length));
                                    out.write(keyBytes);
                                    // Value
                                    out.write(ByteTools.intToByteArray(value.length));
                                    out.write(value);
                                }
                            }

                            if(scan.isCompleteIteration()){
                                break;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean restoreFromFile(String pathToFile) throws IOException{
        File dumpFile = new File(pathToFile);
        if(!dumpFile.exists()) {
            throw new FileNotFoundException("Unable to find the Redis dump file");
        }
        final Charset serializationCharset = StandardCharsets.UTF_8;
        try(FileInputStream in = new FileInputStream(dumpFile)) {
            // Skip version (version exists for potential future use)
            var skipped = in.skip(2);
            // Get the # of nodes
            final int numNodes = ByteTools.byteArrayToInt(in.readNBytes(4));

            for(int i = 0; i < numNodes; i ++){
                // Get node Id
                short nodeIdLen = ByteTools.byteArrayToShort(in.readNBytes(2));
                String nodeId = new String(in.readNBytes(nodeIdLen), serializationCharset);
                processNodeRestoration(nodeId, in, serializationCharset);
            }
        }
        return true;
    }

    private void processNodeRestoration(String nodeId,
                                        FileInputStream in,
                                        Charset serializationCharset) throws IOException {
        // Get the node connection pool
        var connectionPool = _redisConnections.get(nodeId);
        if(connectionPool == null){
            throw new NoSuchElementException(String.format("Unknown node Id: %s", nodeId));
        }
        // Read number of keys in the node
        final long numKeys = ByteTools.byteArrayToLong(in.readNBytes(8));
        long keysProcessed = 0;
        final int batchSize = 25; // # number of keys to read and restore at a time
        while(keysProcessed < numKeys){
            final long batchLimit = Math.min(numKeys, keysProcessed + batchSize);
            Map<String, byte[]> keyValues = new HashMap<>();
            for(; keysProcessed < batchLimit; keysProcessed++){
                // Read key
                int keyLength = ByteTools.byteArrayToInt(in.readNBytes(4));
                String key = new String(in.readNBytes(keyLength), serializationCharset);
                // Read value
                int valueLength = ByteTools.byteArrayToInt(in.readNBytes(4));
                byte[] value = in.readNBytes(valueLength);
                keyValues.put(key, value);
            }
            Map<String, Response<String>> responseMap = new HashMap<>();
            try(Jedis jedis = connectionPool.getResource()) {
                try(Pipeline p = jedis.pipelined()) {
                    for (var keyValue : keyValues.entrySet()) {
                        Response<String> rResponse = p.restore(keyValue.getKey(), 0, keyValue.getValue());
                        responseMap.put(keyValue.getKey(), rResponse);
                    }
                }
            }
            // Make sure all were restored
            var failedKeys = responseMap.entrySet()
                                        .stream()
                                        .filter(e -> !e.getValue().get().equals("OK"))
                                        .map(Map.Entry::getKey).collect(Collectors.toSet());
            if(failedKeys.size() > 0) {
                throw new IOException(
                        String.format("Failed to restore for node '%s' following keys: %s",
                                        nodeId,
                                        String.join(",", failedKeys)));
            }
        }
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
