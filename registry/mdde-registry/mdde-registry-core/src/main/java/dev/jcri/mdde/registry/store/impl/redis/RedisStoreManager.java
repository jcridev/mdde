package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.IStoreManager;
import dev.jcri.mdde.registry.utility.ByteTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanResult;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RedisStoreManager implements IStoreManager {
    private static final Logger logger = LogManager.getLogger(RedisStoreManager.class);

    private RedisConnectionHelper _redisConnection;

    public RedisStoreManager(RedisConnectionHelper redisConnectionHelper){
        Objects.requireNonNull(redisConnectionHelper, "Redis connection helper class can't be set tu null");
        _redisConnection = redisConnectionHelper;
    }


    private static final Charset _dumpCharset = StandardCharsets.UTF_8;
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

        try(FileOutputStream out = new FileOutputStream(dumpFile)) {
            try (FileLock outLock = out.getChannel().lock()) {
                try(Jedis jedis = _redisConnection.getRedisCommands()){
                    // |number of keys in the registry instance of Redis|
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
                                var keyBytes = key.getBytes(_dumpCharset);
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
        return true;
    }

    @Override
    public boolean restoreFromFile(String pathToFile) throws IOException {
        File dumpFile = new File(pathToFile);
        if(!dumpFile.exists()) {
            throw new FileNotFoundException("Unable to find the Redis dump file");
        }

        try(FileInputStream in = new FileInputStream(dumpFile)) {
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
                    String key = new String(in.readNBytes(keyLength), _dumpCharset);
                    // Read value
                    int valueLength = ByteTools.byteArrayToInt(in.readNBytes(4));
                    byte[] value = in.readNBytes(valueLength);
                    keyValues.put(key, value);
                }
                Map<String, Response<String>> responseMap = new HashMap<>();
                try(Jedis jedis = _redisConnection.getRedisCommands()) {
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
                            String.format("Failed to restore for following keys: %s",
                                            String.join(",", failedKeys)));
                }
            }
        }
        return true;
    }

    @Override
    public boolean assignDefaultSnapshot(String snapshotId) {
        if(snapshotId == null || snapshotId.isBlank()){
            // Erase the default key
            try(Jedis jedis = _redisConnection.getRedisCommands()){
                jedis.del(Constants.DEFAULT_SNAPSHOT_ID_KEY);
                return true;
            }
        }
        else{
            try(Jedis jedis = _redisConnection.getRedisCommands()){
                String res = jedis.set(Constants.DEFAULT_SNAPSHOT_ID_KEY, snapshotId);
                if(res.toLowerCase().equals("ok")){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDefaultSnapshotId() {
        try(Jedis jedis = _redisConnection.getRedisCommands()){
            return jedis.get(Constants.DEFAULT_SNAPSHOT_ID_KEY);
        }
    }

    @Override
    public boolean flushAllData() {
        try(Jedis jedis = _redisConnection.getRedisCommands()){
            var res = jedis.flushAll();
            return res.toLowerCase().equals("ok");
        }
    }
}
