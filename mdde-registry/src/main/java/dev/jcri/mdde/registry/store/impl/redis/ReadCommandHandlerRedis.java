package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.ReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import dev.jcri.mdde.registry.store.response.FullRegistry;
import dev.jcri.mdde.registry.store.response.serialization.IResponseSerializer;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.JedisCommands;

import java.util.*;

public class ReadCommandHandlerRedis<T> extends ReadCommandHandler<T> {
    private JedisCommands _redisConnection;

    public ReadCommandHandlerRedis(IResponseSerializer<T> serializer, ConfigRedis config){
        super(serializer);
        Objects.requireNonNull(config, "Redis configuration must be set for the reader");

        _redisConnection = config.getRedisConnection();
        ((Jedis)_redisConnection).connect();
    }

    @Override
    public FullRegistry getFullRegistry() throws ReadOperationException {
        var allFragmentsContents = new HashMap<String, Set<String>>();
        for(String fragmentId: getAllFragmentIds()){
            allFragmentsContents.put(fragmentId, getFragmentTuples(fragmentId));
        }
        // Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
        Map<String, Map<String, Set<String>>> temp = new HashMap<String, Map<String, Set<String>>>();
        var nodes = getNodes();
        for(String nodeId: nodes){
            var nodeMap = new HashMap<String, Set<String>>();
            var nodeFragments = getNodeFragments(nodeId);
            for(String fragmentId: nodeFragments){
                Set<String> fragTuples = allFragmentsContents.get(fragmentId);
                nodeMap.put(fragmentId, fragTuples);
            }
            temp.put(nodeId, nodeMap);
        }
        return new FullRegistry(temp);
    }

    @Override
    public Set<String> getTupleNodes(String tupleId) {
        String containingFragment = getTupleFragment(tupleId);
        if(containingFragment == null){
            return new HashSet<String>();
        }
        return getFragmentNodes(containingFragment);
    }

    @Override
    public String getTupleFragment(String tupleId) {
        var fragments = _redisConnection.smembers(Constants.FRAGMENT_SET);
        String containingFragment = null;
        for(String fragmentId: fragments){
            if(_redisConnection.sismember(Constants.FRAGMENT_PREFIX + fragmentId, tupleId)){
                containingFragment = fragmentId;
                break;
            }
        }
        return containingFragment;
    }

    @Override
    public Set<String> getFragmentNodes(String fragmentId) {
        var nodes = _redisConnection.smembers(Constants.NODES_SET);
        Set<String> result = new HashSet<>();
        for(String nodeId: nodes){
            if(_redisConnection.sismember(Constants.NODE_PREFIX + nodeId, fragmentId)){
                result.add(nodeId);
            }
        }
        return result;
    }

    @Override
    public Set<String> getNodeFragments(String nodeId) {
        return _redisConnection.smembers(Constants.NODE_PREFIX + nodeId);
    }

    @Override
    public Set<String> getFragmentTuples(String fragmentId) throws ReadOperationException {
        Boolean isInSet = _redisConnection.sismember(Constants.FRAGMENT_SET, fragmentId);
        if(!isInSet){
            throw new ReadOperationException(String.format("Fragment '%s' can't be found in %s",
                    fragmentId, Constants.FRAGMENT_SET));
        }
        final String fragmentSet = Constants.FRAGMENT_PREFIX + fragmentId;
        if(!_redisConnection.exists(fragmentSet)){
            throw new ReadOperationException(String.format("Fragment set '%s' does not exist",
                    fragmentSet));
        }
        return _redisConnection.smembers(fragmentSet);
    }

    @Override
    public int getCountFragment(String fragmentId) {
        return getFragmentNodes(fragmentId).size();
    }

    @Override
    public int getCountTuple(String tupleId) {
        String containingFragment = getTupleFragment(tupleId);
        if(containingFragment == null){
            return 0;
        }
        return getFragmentNodes(containingFragment).size();
    }

    @Override
    public Set<String> getNodes() {
        return _redisConnection.smembers(Constants.NODES_SET);
    }

    @Override
    public boolean getIsNodeExists(String nodeId) {
        return _redisConnection.sismember(Constants.NODES_SET, nodeId);
    }

    @Override
    public Set<String> getAllFragmentIds() {
        return _redisConnection.smembers(Constants.FRAGMENT_SET);
    }
}
