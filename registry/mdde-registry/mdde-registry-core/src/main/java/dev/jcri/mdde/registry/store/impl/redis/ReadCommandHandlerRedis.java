package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.store.impl.ReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.*;
import java.util.stream.Collectors;

public class ReadCommandHandlerRedis extends ReadCommandHandler {
    private static final Logger logger = LogManager.getLogger(ReadCommandHandler.class);

    private RedisConnectionHelper _redisConnection;

    public ReadCommandHandlerRedis(RedisConnectionHelper redisConnectionHelper){
        Objects.requireNonNull(redisConnectionHelper, "Redis connection helper class can't be set tu null");
        _redisConnection = redisConnectionHelper;
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

    @Override
    public Set<String> runGetAllNodeTuples(String nodeId) throws ReadOperationException {
        var unassignedTuples = runGetUnassignedTuples(nodeId);
        // Unassigned tuples
        var result = new HashSet<>(unassignedTuples);
        // Tuples assigned to fragments
        var nodeFragments = runGetNodeFragments(nodeId);
        for(String fragmentId: nodeFragments){
            result.addAll(runGetFragmentTuples(fragmentId));
        }
        return result;
    }

    @Override
    protected FragmentCatalog runGetFragmentCatalog(Set<String> metaTagsExemplar, Set<String> metaTagsGlobal) {
        final var nodes = runGetNodes();

        List<String> resultLocalMetaTags = new ArrayList<>(metaTagsExemplar);
        List<String> resultGlobalMetaTags = new ArrayList<>(metaTagsGlobal);
        // <local fragment key map value, array of expected
        Map<Integer, List<String>> resultGlobalFragmentTagValues= new HashMap<>();
        // <Node local Id, <Fragment local id, Meta values>>
        Map<Integer, Map<Integer, List<String>>> resultLocalFragmentTagValues = new HashMap<>();

        Map<String, Integer> resultNodes = new HashMap<>();
        Map<String, Integer> resultFragments = new HashMap<>();
        Map<Integer, List<Integer>> resultNodesFragments = new HashMap<>();

        try(var jedis = _redisConnection.getRedisCommands()) {
            // Retrieve fragments
            HashMap<String, Response<Set<String>>> nodeFragmentsQRes = new HashMap<>();
            try(Pipeline p = jedis.pipelined()){
                for(var nodeId: nodes) {
                    nodeFragmentsQRes.put(nodeId, p.smembers(Constants.NODE_PREFIX + nodeId));
                }
                p.sync();
            }

            int nodeIdMappingKey = 0;
            int fragmentIdMappingKey = 0;
            for(var nodeFragments: nodeFragmentsQRes.entrySet()){
                int currentNodeIde = nodeIdMappingKey;
                resultNodes.put(nodeFragments.getKey(), nodeIdMappingKey++);
                List<Integer> nodeFragmentLocalKeyMap = new ArrayList<>();
                var fragments = nodeFragments.getValue().get();
                if(fragments.size() == 0){
                    continue;
                }
                for (String fragId : fragments) {
                    Integer localId = resultFragments.get(fragId);
                    if (localId == null) {
                        localId = fragmentIdMappingKey++;
                        resultFragments.put(fragId, localId);
                    }
                    nodeFragmentLocalKeyMap.add(localId);
                }
                resultNodesFragments.put(currentNodeIde, nodeFragmentLocalKeyMap);
                // <Fragment local id, Meta values>
                HashMap<Integer, List<String>> fragmentMeta = new HashMap<>();
                resultLocalFragmentTagValues.put(currentNodeIde, fragmentMeta);
                if(resultLocalMetaTags.size() > 0) {
                    // Get exemplar meta
                    for (String fragId : fragments) {
                        var currentFragmentMetaValues = new ArrayList<String>();
                        fragmentMeta.put(resultFragments.get(fragId), currentFragmentMetaValues);
                        var fragMetaKey = CommandHandlerRedisHelper.sharedInstance()
                                .genExemplarFragmentMetaFieldName(fragId, nodeFragments.getKey());
                        // TODO: Batch pipelining
                        for(var localTag: resultLocalMetaTags){
                            currentFragmentMetaValues.add(jedis.hget(fragMetaKey, localTag));
                        }
                    }
                }
            }

            if(resultGlobalMetaTags.size() > 0) {
                // Global fragments // TODO: Batch pipelining
                for (var fragIdAndLocalKey : resultFragments.entrySet()) {
                    var gMetaKeyFrag = CommandHandlerRedisHelper.sharedInstance()
                            .genGlobalFragmentMetaFieldName(fragIdAndLocalKey.getKey());
                    List<String> currentFragGMeta = new ArrayList<>();
                    resultGlobalFragmentTagValues.put(fragIdAndLocalKey.getValue(), currentFragGMeta);
                    for (var globalTag : resultGlobalMetaTags) {
                        currentFragGMeta.add(jedis.hget(gMetaKeyFrag, globalTag));
                    }
                }
            }
        }
        return new FragmentCatalog(resultNodes,
                resultFragments,
                resultNodesFragments,
                resultLocalMetaTags,
                resultGlobalMetaTags,
                resultGlobalFragmentTagValues,
                resultLocalFragmentTagValues);
    }

    private Set<String> getUnassignedTupleNodes(String tupleId){
        var nodes = runGetNodes();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try (var p = jedis.pipelined()) {
                var tempRes = new HashMap<String, Response<Boolean>>();
                for (String nodeId : nodes) {
                    tempRes.put(nodeId, p.sismember(Constants.NODE_HEAP_PREFIX + nodeId, tupleId));
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
            return jedis.smembers(Constants.NODE_HEAP_PREFIX + nodeId);
        }
    }

    @Override
    public Set<String> runGetAllFragmentIds() {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.smembers(Constants.FRAGMENT_SET);
        }
    }

    @Override
    protected String runGetGlobalFragmentMeta(String fragmentId, String metaName) {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.hget(CommandHandlerRedisHelper.sharedInstance().genGlobalFragmentMetaFieldName(fragmentId), metaName);
        }
    }

    @Override
    protected String runGetExemplarFragmentMeta(String fragmentId, String nodeId, String metaName) {
        try (var jedis = _redisConnection.getRedisCommands()) {
            return jedis.hget(CommandHandlerRedisHelper.sharedInstance().genExemplarFragmentMetaFieldName(fragmentId, nodeId), metaName);
        }
    }

    @Override
    public Boolean runGetIsTuplesUnassigned(String nodeId, Set<String> tupleIds) {
        Map<String, Response<Boolean>> responses = new HashMap<>();
        try(var jedis = _redisConnection.getRedisCommands()) {
            try (var p = jedis.pipelined()) {

                for (String tupleId : tupleIds) {
                    responses.put(tupleId, p.sismember(Constants.NODE_HEAP_PREFIX + nodeId, tupleId));
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
