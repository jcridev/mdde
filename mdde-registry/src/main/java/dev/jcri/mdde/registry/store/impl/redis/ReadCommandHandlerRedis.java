package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.impl.ReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.response.FullRegistry;
import redis.clients.jedis.Response;

import java.util.*;
import java.util.stream.Collectors;

public class ReadCommandHandlerRedis extends ReadCommandHandler {
    private RedisConnectionHelper _redisConnection;

    public ReadCommandHandlerRedis(){
        _redisConnection = RedisConnectionHelper.getInstance();
    }

    @Override
    public FullRegistry runGetFullRegistry() throws ReadOperationException {
        var allFragmentsContents = new HashMap<String, Set<String>>();
        for(String fragmentId: runGetAllFragmentIds()){
            allFragmentsContents.put(fragmentId, runGetFragmentTuples(fragmentId));
        }
        // Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
        Map<String, Map<String, Set<String>>> temp = new HashMap<String, Map<String, Set<String>>>();
        var nodes = runGetNodes();
        for(String nodeId: nodes){
            var nodeMap = new HashMap<String, Set<String>>();
            var nodeFragments = runGetNodeFragments(nodeId);
            for(String fragmentId: nodeFragments){
                Set<String> fragTuples = allFragmentsContents.get(fragmentId);
                nodeMap.put(fragmentId, fragTuples);
            }
            temp.put(nodeId, nodeMap);
        }
        return new FullRegistry(temp);
    }

    private Set<String> getUnassignedTupleNodes(String tupleId){
        var nodes = runGetNodes();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try (var p = jedis.pipelined()) {
                var tempRes = new HashMap<String, Response<Boolean>>();
                for (String nodeId : nodes) {
                    tempRes.put(nodeId, p.sismember(Constants.NODE_HEAP + nodeId, tupleId));
                }
                p.sync();

                return tempRes.entrySet()
                        .stream()
                        .filter(x -> x.getValue().get())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
            }
        }
    }

    @Override
    public Set<String> runGetTupleNodes(String tupleId) {
        String containingFragment = runGetTupleFragment(tupleId);
        if(containingFragment == null){
            return getUnassignedTupleNodes(tupleId);
        }
        return runGetFragmentNodes(containingFragment);
    }

    @Override
    public String runGetTupleFragment(String tupleId) {
        try (var jedis = _redisConnection.getRedisCommands()) {
            var fragments = jedis.smembers(Constants.FRAGMENT_SET);
            String containingFragment = null;
            for (String fragmentId : fragments) {
                if (jedis.sismember(Constants.FRAGMENT_PREFIX + fragmentId, tupleId)) {
                    containingFragment = fragmentId;
                    break;
                }
            }

            return containingFragment;
        }
    }

    @Override
    public Set<String> runGetFragmentNodes(String fragmentId) {
        try (var jedis = _redisConnection.getRedisCommands()) {
            var nodes = jedis.smembers(Constants.NODES_SET);
            Set<String> result = new HashSet<>();
            for (String nodeId : nodes) {
                if (jedis.sismember(Constants.NODE_PREFIX + nodeId, fragmentId)) {
                    result.add(nodeId);
                }
            }
            return result;
        }
    }

    @Override
    public Set<String> runGetNodeFragments(String nodeId) {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.smembers(Constants.NODE_PREFIX + nodeId);
        }
    }

    @Override
    public Set<String> runGetFragmentTuples(String fragmentId) throws ReadOperationException {
        try (var jedis = _redisConnection.getRedisCommands()) {
            Boolean isInSet = jedis.sismember(Constants.FRAGMENT_SET, fragmentId);
            if (!isInSet) {
                throw new ReadOperationException(String.format("Fragment '%s' can't be found in %s",
                        fragmentId, Constants.FRAGMENT_SET));
            }
            final String fragmentSet = Constants.FRAGMENT_PREFIX + fragmentId;
            if (!jedis.exists(fragmentSet)) {
                throw new ReadOperationException(String.format("Fragment set '%s' does not exist",
                        fragmentSet));
            }
            return jedis.smembers(fragmentSet);
        }
    }

    @Override
    public int runGetCountFragment(String fragmentId) {
        return runGetFragmentNodes(fragmentId).size();
    }

    @Override
    public int runGetCountTuple(String tupleId) {
        String containingFragment = runGetTupleFragment(tupleId);
        if(containingFragment == null){
            return 0;
        }
        return runGetFragmentNodes(containingFragment).size();
    }

    @Override
    public Set<String> runGetNodes() {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.smembers(Constants.NODES_SET);
        }
    }

    @Override
    public boolean runGetIsNodeExists(String nodeId) {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.sismember(Constants.NODES_SET, nodeId);
        }
    }

    @Override
    public Set<String> runGetUnassignedTuples(String nodeId) {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.smembers(Constants.NODE_HEAP + nodeId);
        }
    }

    @Override
    public Set<String> runGetAllFragmentIds() {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.smembers(Constants.FRAGMENT_SET);
        }
    }

    @Override
    public Boolean runGetIsTuplesUnassigned(String nodeId, Set<String> tupleIds) {
        Map<String, Response<Boolean>> responses = new HashMap<>();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try (var p = jedis.pipelined()) {

                for (String tupleId : tupleIds) {
                    responses.put(tupleId, p.sismember(Constants.NODE_HEAP + nodeId, tupleId));
                }
                p.sync();
            }
        }

        return responses.entrySet().stream().allMatch(x -> x.getValue().get());
    }

    @Override
    public Boolean runGetIsNodeContainsFragment(String nodeId, String fragmentId) {
        try(var jedis = _redisConnection.getRedisCommands()) {
            return jedis.sismember(Constants.NODE_PREFIX + nodeId, fragmentId);
        }
    }
}
