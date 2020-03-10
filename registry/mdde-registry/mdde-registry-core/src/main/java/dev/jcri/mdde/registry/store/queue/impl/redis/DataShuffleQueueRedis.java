package dev.jcri.mdde.registry.store.queue.impl.redis;

import dev.jcri.mdde.registry.store.impl.redis.Constants;
import dev.jcri.mdde.registry.store.impl.redis.RedisConnectionHelper;
import dev.jcri.mdde.registry.store.queue.IDataShuffleQueue;
import dev.jcri.mdde.registry.store.queue.actions.DataAction;
import dev.jcri.mdde.registry.utility.IteratorThrowing;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A queue backed by Redis
 */
public class DataShuffleQueueRedis implements IDataShuffleQueue {
    private final RedisConnectionHelper _redisConnectionHelper;
    private final String _redisListKey;

    /**
     * Constructor (using default queue Registry key)
     * @param redisConnectionHelper Initialized Redis connection pool to the Registry
     */
    public DataShuffleQueueRedis(RedisConnectionHelper redisConnectionHelper){
        this(redisConnectionHelper, Constants.DATA_SHUFFLE_QUEUE_KEY);
    }

    /**
     * Constructor
     * @param redisConnectionHelper Initialized Redis connection pool to the Registry
     * @param redisListKey Key in the specified Redis instance that holds the queue
     */
    public DataShuffleQueueRedis(RedisConnectionHelper redisConnectionHelper, String redisListKey){
        Objects.requireNonNull(redisConnectionHelper, "Redis connection helper can't be null");
        if(redisListKey == null || redisListKey.isBlank()){
            throw new IllegalArgumentException("redisListKey can't be null or empty");
        }
        _redisConnectionHelper = redisConnectionHelper;
        _redisListKey = redisListKey;
    }


    @Override
    public int size() throws IOException {
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            long queueLength = jedis.llen(_redisListKey);
            return Math.toIntExact(queueLength);
        }
    }

    @Override
    public boolean isEmpty() throws IOException {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) throws IOException {
        var it = this.iterator();
        while (it.hasNext()){
            var item = it.next();
            if(item.equals(o)){
                return true;
            }
        }
        return false;
    }

    @Override
    public IteratorThrowing<DataAction> iterator() {
        IteratorThrowing<DataAction> it = new IteratorThrowing<DataAction>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() throws IOException  {
                try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
                    return currentIndex < Math.toIntExact(jedis.llen(_redisListKey));
                }
            }

            @Override
            public DataAction next() throws IOException {
                try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
                    var element = jedis.lindex(_redisListKey, currentIndex++);
                    return Serializer.deserialize(element);
                }
            }
        };
        return it;
    }

    @Override
    public Object[] toArray() throws IOException {
        return this.toArray(new DataAction[0]);
    }

    @Override
    public DataAction[] toArray(DataAction[] a) throws IOException {
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            var allElements = jedis.lrange(_redisListKey, 0, -1);
            if(allElements == null || allElements.size() == 0){
                return a;
            }
            DataAction[] result = new DataAction[allElements.size()];
            System.arraycopy(a, 0, result, 0, 0);
            for (int i = 0; i < allElements.size(); i++) {
                String element = allElements.get(i);
                result[i] = Serializer.deserialize(element);
            }
            return result;
        }
    }

    @Override
    public boolean add(DataAction dataAction) throws IOException {
        var serialized = Serializer.serialize(dataAction);
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            var res = jedis.rpush(_redisListKey, serialized);
            if(res > 0){
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() throws IOException {
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            jedis.del(_redisListKey);
        }
    }

    @Override
    public DataAction remove() throws IOException {
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            // get the left most element
            var element = jedis.lpop(_redisListKey);
            if(element == null){
                throw new NoSuchElementException();
            }
            return Serializer.deserialize(element);
        }
    }

    @Override
    public DataAction poll() throws IOException {
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            // get the left most element
            var element = jedis.lpop(_redisListKey);
            if(element == null){
                return null;
            }
            return Serializer.deserialize(element);
        }
    }

    @Override
    public DataAction element() throws IOException {
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            // get the left most element
            var elements = jedis.lrange(_redisListKey, 0,0);
            if(elements == null || elements.size() == 0){
                throw new NoSuchElementException();
            }
            return Serializer.deserialize(elements.get(0));
        }
    }

    @Override
    public DataAction peek() throws IOException {
        try(Jedis jedis = _redisConnectionHelper.getRedisCommands()){
            // get the left most element
            var elements = jedis.lrange(_redisListKey, 0,0);
            if(elements == null || elements.size() == 0){
                return null;
            }
            return Serializer.deserialize(elements.get(0));
        }
    }
}
