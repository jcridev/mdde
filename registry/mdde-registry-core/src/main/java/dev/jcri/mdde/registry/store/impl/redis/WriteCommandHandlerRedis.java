package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.impl.ReadCommandHandler;
import dev.jcri.mdde.registry.store.impl.WriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.RegistryEntityType;
import dev.jcri.mdde.registry.store.exceptions.UnknownEntityIdException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;

import java.util.*;

public class WriteCommandHandlerRedis extends WriteCommandHandler {
    private static final Logger logger = LogManager.getLogger(WriteCommandHandler.class);

    private RedisConnectionHelper _redisConnection;

    public WriteCommandHandlerRedis(RedisConnectionHelper redisConnectionHelper, IReadCommandHandler readCommandHandler) {
        super(readCommandHandler);
        Objects.requireNonNull(redisConnectionHelper, "Redis connection helper class can't be set tu null");
        _redisConnection = redisConnectionHelper;
    }

    @Override
    protected void runInsertTupleToNode(String tupleId, String nodeId) throws WriteOperationException {
        try(Jedis jedis = _redisConnection.getRedisCommands()) {
            var added = jedis.sadd(Constants.NODE_HEAP + nodeId, tupleId);
            if (added < 0) {
                throw new WriteOperationException(String.format("Failed to add %s", tupleId));
            }
        }
    }

    @Override
    protected void runInsertTupleToNode(Set<String> tupleIds, String nodeId) throws WriteOperationException {
        Map<String, Response<Long>> responses = new HashMap<>();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try (var p = jedis.pipelined()) {
                for (String tupleId : tupleIds) {
                    Response<Long> r = p.sadd(Constants.NODE_HEAP + nodeId, tupleId);
                    responses.put(nodeId, r);
                }
                p.sync();
            }
        }
        for (Map.Entry<String, Response<Long>> r : responses.entrySet()) {
            if (r.getValue().get() == 0) {
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }
    }

    @Override
    protected void runInsertTupleToFragment(String tupleId, String fragmentId) throws WriteOperationException {
        try(Jedis jedis = _redisConnection.getRedisCommands()) {
            var added = jedis.sadd(Constants.FRAGMENT_PREFIX + fragmentId, tupleId);
            if (added < 0) {
                throw new WriteOperationException(String.format("Failed to add %s", tupleId));
            }
        }
    }

    @Override
    protected void runInsertTupleToFragment(Set<String> tupleIds, String fragmentId) throws WriteOperationException {
        Map<String, Response<Long>> responses = new HashMap<>();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try(var p = jedis.pipelined()) {
                for (String tupleId : tupleIds) {
                    Response<Long> r = p.sadd(Constants.FRAGMENT_PREFIX + fragmentId, tupleId);
                    responses.put(fragmentId, r);
                }
                p.sync();
            }
        }

        for(Map.Entry<String, Response<Long>> r: responses.entrySet()){
            if (r.getValue().get() == 0){
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }
    }

    private Boolean removeUnassignedTupleFromAllNodes(String... tupleIds){
        var nodes = readCommandHandler.getNodes();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try(var p = jedis.pipelined()) {
                for (String nodeId : nodes) {
                    p.srem(Constants.NODE_HEAP + nodeId, tupleIds);
                }
                var tempRes = p.syncAndReturnAll();
                return tempRes.stream().anyMatch(x -> ((long) x > 0));
            }
        }
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
    protected String runFormFragment(final Set<String> tupleIds, String fragmentId, String nodeId) throws WriteOperationException {
        final String keyFragment = Constants.FRAGMENT_PREFIX + fragmentId;
        Map<String, Response<Long>> responses = new HashMap<>();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try(var t = _redisConnection.getTransaction(jedis, Collections.singleton(keyFragment))) {
                t.sadd(Constants.FRAGMENT_SET, fragmentId);
                t.sadd(Constants.NODE_PREFIX + nodeId, fragmentId);

                for (String tupleId : tupleIds) {
                    Response<Long> r = t.sadd(keyFragment, tupleId);
                    responses.put(tupleId, r);
                }

                var res = t.exec();
            }
        }
        for(Map.Entry<String, Response<Long>> r: responses.entrySet()){
            if (r.getValue().get() == 0){
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }
        return "";
    }

    @Override
    protected void runAppendTupleToFragment(String tupleId, String fragmentId) throws WriteOperationException {
        try(var jedis = _redisConnection.getRedisCommands()) {
            var added = jedis.sadd(Constants.FRAGMENT_PREFIX + fragmentId, tupleId);
            if (added < 1) {
                throw new WriteOperationException(String.format("Failed to add tuple %s to fragment %s", tupleId, fragmentId));
            }
        }
    }

    @Override
    protected void runReplicateFragment(String fragmentId, String sourceNodeId, String destinationNodeId) throws WriteOperationException {
        try(var jedis = _redisConnection.getRedisCommands()) {
            var key = Constants.NODE_PREFIX + destinationNodeId;
            var metaKeySource = CommandHandlerRedisHelper.getInstance().genExemplarFragmentMetaFieldName(fragmentId, sourceNodeId);
            var metaKeyDest = CommandHandlerRedisHelper.getInstance().genExemplarFragmentMetaFieldName(fragmentId, destinationNodeId);
            var sourceMeta = jedis.hgetAll(metaKeySource);
            Response<Long> added;
            Response<Long> addedMeta;
            try(var t = _redisConnection.getTransaction(jedis, key, metaKeySource)){
                added = t.sadd(Constants.NODE_PREFIX + destinationNodeId, fragmentId);
                if(sourceMeta != null && sourceMeta.size() > 0){
                    addedMeta = t.hset(metaKeyDest, sourceMeta);
                }
                t.exec();
            }
            if (added.get() < 1) {
                throw new WriteOperationException(String.format("Failed to add fragment exemplar %s to node %s", fragmentId, destinationNodeId));
            }
        }
    }

    @Override
    protected void runDeleteFragmentExemplar(String fragmentId, String nodeId) throws WriteOperationException {
        try(var jedis = _redisConnection.getRedisCommands()) {
            var fragmentERemoved = jedis.srem(Constants.NODE_PREFIX + nodeId, fragmentId);
            if (fragmentERemoved < 1) {
                throw new WriteOperationException(String.format("Failed to remove fragment %s from node %s", fragmentId, nodeId));
            }
            // Remove meta
            var metaKey = CommandHandlerRedisHelper.getInstance().genExemplarFragmentMetaFieldName(fragmentId, nodeId);
            jedis.del(metaKey);
        }
    }

    @Override
    protected String runCompleteFragmentDeletion(String fragmentId) {
        return null;
    }

    @Override
    protected boolean runPopulateNodes(Set<String> nodeIds) throws WriteOperationException {
        Map<String, Response<Long>> responses = new HashMap<>();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try(var p = jedis.pipelined()) {

                for (String nodeId : nodeIds) {
                    Response<Long> r = p.sadd(Constants.NODES_SET, nodeId);
                    responses.put(nodeId, r);
                }
                p.sync();
            }
        }
        for(Map.Entry<String, Response<Long>> r: responses.entrySet()){
            if (r.getValue().get() == 0){
                throw new WriteOperationException(String.format("Failed to add %s", r.getKey()));
            }
        }
        return true;
    }

    @Override
    protected void runAddMetaToFragmentGlobal(String fragmentId, String metaField, String metaValue)
            throws WriteOperationException {
        try(var jedis = _redisConnection.getRedisCommands()) {
            var key = CommandHandlerRedisHelper.getInstance().genGlobalFragmentMetaFieldName(fragmentId);
            if (metaValue != null) {
                var added = jedis.hset(key, metaField, metaValue);
                if (added < 1) {
                    throw new WriteOperationException(
                            String.format("Failed to add a global meta field %s to fragment %s", metaField, fragmentId)
                    );
                }
            }
            else{
                var removed = jedis.hdel(key, metaField);
                if (removed < 1) {
                    logger.info(String.format("Failed to remove a global meta field %s to fragment %s", metaField, fragmentId));
                }
            }
        }
    }

    @Override
    protected void runAddMetaToFragmentExemplar(String fragmentId, String nodeId, String metaField, String metaValue)
            throws WriteOperationException {

        var key = CommandHandlerRedisHelper.getInstance().genExemplarFragmentMetaFieldName(fragmentId, nodeId);
        try(var jedis = _redisConnection.getRedisCommands()) {

            if (metaValue != null) {
                var added = jedis.hset(key, metaField, metaValue);
                if (added < 1) {
                    throw new WriteOperationException(
                            String.format("Failed to add a meta field %s to fragment %s located on node %s", metaField, fragmentId, nodeId)
                    );
                }
            }
            else{
                var removed = jedis.hdel(key, metaField);
                if (removed < 1) {
                    logger.info(String.format("Failed to remove a meta field %s to fragment %s located on node %s", metaField, fragmentId, nodeId));
                }
            }
        }
    }

    @Override
    protected void runResetFragmentsMeta() {
        try(var jedis = _redisConnection.getRedisCommands()) {
            jedis.del(Constants.FRAGMENT_GLOBAL_META_PREFIX + "*");
            jedis.del(Constants.FRAGMENT_EXEMPLAR_META_PREFIX + "*");
        }
    }

    @Override
    protected void runFlush() {
        try(var jedis = _redisConnection.getRedisCommands()) {
            jedis.flushAll();
        }
    }
}
