package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.ReadCommandHandler;
import dev.jcri.mdde.registry.store.WriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.RegistryEntityType;
import dev.jcri.mdde.registry.store.exceptions.UnknownEntityIdException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import dev.jcri.mdde.registry.store.response.serialization.IResponseSerializer;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.commands.JedisCommands;

import java.util.*;
import java.util.stream.Collectors;

public class WriteCommandHandlerRedis<T> extends WriteCommandHandler<T> {
    private RedisConnectionHelper _redisConnection;

    public WriteCommandHandlerRedis(ReadCommandHandler<T> readCommandHandler,
                                    IResponseSerializer<T> serializer,
                                    ConfigRedis config) {
        super(readCommandHandler, serializer);

        Objects.requireNonNull(config, "Redis configuration must be set for the writer");

        _redisConnection = new RedisConnectionHelper(config);
        _redisConnection.getRedisCommands();
    }

    @Override
    protected void runInsertTupleToNode(String tupleId, String nodeId) throws WriteOperationException {
        var added = _redisConnection.getRedisCommands().sadd(Constants.NODE_HEAP + nodeId, tupleId);
        if(added < 0){
            throw new WriteOperationException(String.format("Failed to add %s", tupleId));
        }
    }

    @Override
    protected void runInsertTupleToNode(Set<String> tupleIds, String nodeId) throws WriteOperationException {
        var p = _redisConnection.getPipeline();
        Map<String, Response<Long>> responses = new HashMap<>();
        for(String tupleId: tupleIds){
            Response<Long> r = p.sadd(Constants.NODE_HEAP + nodeId, tupleId);
            responses.put(nodeId, r);
        }
        p.sync();

        for(Map.Entry<String, Response<Long>> r: responses.entrySet()){
            if (r.getValue().get() == 0){
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }
    }

    @Override
    protected void runInsertTupleToFragment(String tupleId, String fragmentId) throws WriteOperationException {
        var added = _redisConnection.getRedisCommands().sadd(Constants.FRAGMENT_PREFIX + fragmentId, tupleId);
        if(added < 0){
            throw new WriteOperationException(String.format("Failed to add %s", tupleId));
        }
    }

    @Override
    protected void runInsertTupleToFragment(Set<String> tupleIds, String fragmentId) throws WriteOperationException {
        var p = _redisConnection.getPipeline();
        Map<String, Response<Long>> responses = new HashMap<>();
        for(String tupleId: tupleIds){
            Response<Long> r = p.sadd(Constants.FRAGMENT_PREFIX + fragmentId, tupleId);
            responses.put(fragmentId, r);
        }
        p.sync();

        for(Map.Entry<String, Response<Long>> r: responses.entrySet()){
            if (r.getValue().get() == 0){
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }
    }

    private Boolean removeUnassignedTupleFromAllNodes(String... tupleIds){
        var nodes = readCommandHandler.getNodes();
        var p = _redisConnection.getPipeline();
        for(String nodeId: nodes){
            p.srem(Constants.NODE_HEAP  + nodeId, tupleIds);
        }
        var tempRes = p.syncAndReturnAll();
        return tempRes.stream().anyMatch(x -> ((long)x > 0));
    }

    @Override
    protected void runCompleteTupleDeletion(String tupleId) throws UnknownEntityIdException, WriteOperationException {
        var fragmentId = readCommandHandler.getTupleFragment(tupleId);
        if(fragmentId == null){
            if(!removeUnassignedTupleFromAllNodes(tupleId)){
                throw new UnknownEntityIdException(RegistryEntityType.Tuple, tupleId);
            }
        }
        else {
            var numRem = _redisConnection.getRedisCommands().srem(Constants.FRAGMENT_PREFIX + fragmentId, tupleId);
            if (numRem < 1) {
                throw new WriteOperationException(String.format("Failed to remove tuple %s from fragment %s",
                        tupleId, fragmentId));
            }
        }
    }

    @Override
    protected String runFormFragment(Set<String> tupleIds, String fragmentId) throws WriteOperationException {
        var t = _redisConnection.getTransaction(Collections.singleton(Constants.FRAGMENT_PREFIX + fragmentId));
        Map<String, Response<Long>> responses = new HashMap<>();
        for(String tupleId: tupleIds){
            Response<Long> r = t.sadd(Constants.FRAGMENT_PREFIX + fragmentId,  tupleId);
            responses.put(tupleId, r);
        }
        var res = t.save();

        for(Map.Entry<String, Response<Long>> r: responses.entrySet()){
            if (r.getValue().get() == 0){
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }
        return res.get();
    }

    @Override
    protected void runAppendTupleToFragment(String tupleId, String fragmentId) throws WriteOperationException {
        var added =_redisConnection.getRedisCommands().sadd(Constants.FRAGMENT_PREFIX + fragmentId, tupleId);
        if(added < 1){
            throw new WriteOperationException(String.format("Failed to add tuple %s to fragment %s", tupleId, fragmentId));
        }
    }

    @Override
    protected void runReplicateFragment(String fragmentId, String sourceNodeId, String destinationNodeId) throws WriteOperationException {
        var added = _redisConnection.getRedisCommands().sadd(Constants.NODE_PREFIX + destinationNodeId, fragmentId);
        if(added < 1){
            throw new WriteOperationException(String.format("Failed to add fragment exemplar %s to node %s", fragmentId, destinationNodeId));
        }
    }

    @Override
    protected void runDeleteFragmentExemplar(String fragmentId, String nodeId) throws WriteOperationException {
        var removed = _redisConnection.getRedisCommands().srem(Constants.NODE_PREFIX + nodeId, fragmentId);
        if(removed < 1){
            throw new WriteOperationException(String.format("Failed to remove fragment %s from node %s", fragmentId, nodeId));
        }
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
