package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.ReadCommandHandler;
import dev.jcri.mdde.registry.store.response.FullRegistry;
import dev.jcri.mdde.registry.store.response.serialization.IResponseSerializer;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.JedisCommands;

import java.util.List;
import java.util.Objects;

public class ReadCommandHandlerRedis<T> extends ReadCommandHandler<T> {
    private final ConfigRedis _redisConfiguration;
    private JedisCommands _redisConnection;

    public ReadCommandHandlerRedis(IResponseSerializer<T> serializer, ConfigRedis config){
        super(serializer);
        Objects.requireNonNull(config, "Redis configuration must be set for the reader");
        _redisConfiguration = config;

        _redisConnection = config.getRedisConnection();
        ((Jedis)_redisConnection).connect();
    }

    @Override
    public FullRegistry getFullRegistry() {
        return null;
    }

    @Override
    public List<String> getTupleNodes(String tupleId) {
        return null;
    }

    @Override
    public String getTupleFragment(String tupleId) {
        return null;
    }

    @Override
    public List<String> getFragmentNodes(String fragmentId) {
        return null;
    }

    @Override
    public List<String> getFragmentTuples(String fragmentId) {
        return null;
    }

    @Override
    public int getCountFragment(String fragmentId) {
        return 0;
    }

    @Override
    public int getCountTuple(String tupleId) {
        return 0;
    }

    @Override
    public List<String> getNodes() {
        return null;
    }

    @Override
    public boolean getIsNodeExists(String nodeId) {
        return false;
    }
}
