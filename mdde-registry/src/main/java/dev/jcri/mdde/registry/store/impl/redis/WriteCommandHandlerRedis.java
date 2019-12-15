package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.ReadCommandHandler;
import dev.jcri.mdde.registry.store.WriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import dev.jcri.mdde.registry.store.response.serialization.IResponseSerializer;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.commands.JedisCommands;

import java.util.*;

public class WriteCommandHandlerRedis<T> extends WriteCommandHandler<T> {
    private final ConfigRedis _redisConfiguration;
    private RedisConnectionHelper _redisConnection;

    public WriteCommandHandlerRedis(ReadCommandHandler<T> readCommandHandler,
                                    IResponseSerializer<T> serializer,
                                    ConfigRedis config) {
        super(readCommandHandler, serializer);

        Objects.requireNonNull(config, "Redis configuration must be set for the writer");
        _redisConfiguration = config;

        _redisConnection = new RedisConnectionHelper(config);
        _redisConnection.getRedisCommands();
    }

    @Override
    protected String runInsertTuple(String tupleId, String nodeId, String fragmentId) {
        return null;
    }

    @Override
    protected String runInsertTuple(List<String> tupleId, String nodeId, String fragmentId) {
        return null;
    }

    @Override
    protected String runDeleteTuple(String tupleId) {
        return null;
    }

    @Override
    protected String runFormFragment(List<String> tupleId, String fragmentId) {
        return null;
    }

    @Override
    protected String runAppendTupleToFragment(String tupleId, String fragmentId) {
        return null;
    }

    @Override
    protected String runReplicateFragment(String fragmentId, String sourceNodeId, String destinationNodeId) {
        return null;
    }

    @Override
    protected String runDeleteFragmentExemplar(String fragmentId, String nodeId) {
        return null;
    }

    @Override
    protected String runCompleteFragmentDeletion(String fragmentId) {
        return null;
    }

    @Override
    protected boolean runPopulateNodes(Set<String> nodeIds) throws WriteOperationException {
        var p = _redisConnection.getPipeline();
        Map<String, Response<Long>> responses = new HashMap<>();
        for(String nodeId: nodeIds){
            Response<Long> r = p.sadd(Constants.NODES_SET, nodeId);
            responses.put(nodeId, r);
        }
        p.sync();

        for(Map.Entry<String, Response<Long>> r: responses.entrySet()){
            if (r.getValue().get() == 0){
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }

        return true;
    }
}
