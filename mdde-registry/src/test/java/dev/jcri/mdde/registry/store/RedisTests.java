package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.exceptions.*;
import dev.jcri.mdde.registry.store.impl.redis.ConfigRedis;
import dev.jcri.mdde.registry.store.impl.redis.ReadCommandHandlerRedis;

import dev.jcri.mdde.registry.store.impl.redis.Constants;
import dev.jcri.mdde.registry.store.impl.redis.RedisConnectionHelper;
import dev.jcri.mdde.registry.store.impl.redis.WriteCommandHandlerRedis;
import dev.jcri.mdde.registry.store.response.serialization.ResponseSerializerPassThrough;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RedisTests {
    /**
     * Creates a reader class that opens a connection in the constructor with the default localhost Redis instance
     * (assuming Redis is running within the system is where this test is executed)
     */
    @Test
    public void testLocalhostConnection(){
        var testConfig = new ConfigRedis();
        var serializer = new ResponseSerializerPassThrough();
        var redisReader = new ReadCommandHandlerRedis<Object>(serializer, testConfig);
    }

    @Test
    public void testTupleLifecycle(){
        var testConfig = new ConfigRedis();
        var serializer = new ResponseSerializerPassThrough();

        if(!RedisConnectionHelper.getInstance().getIsInitialized()){
            RedisConnectionHelper.getInstance().initialize(testConfig);
        }

        var redisReader = new ReadCommandHandlerRedis<Object>(serializer, testConfig);
        var redisWriter = new WriteCommandHandlerRedis<Object>(redisReader, serializer, testConfig);

        final String randNodeId = UUID.randomUUID().toString();
        final String randTupleId = UUID.randomUUID().toString();
        try {
            try {
                redisWriter.populateNodes(Collections.singleton(randNodeId));
            } catch (WriteOperationException e) {
                fail("Failed to create a random node in the catalog", e);
            }

            var currentNodes = redisReader.getNodes();
            assertEquals(1, currentNodes.size());

            try {
                redisWriter.insertTuple(randTupleId, randNodeId);
            } catch (MddeRegistryException e) {
                fail("Failed to add a tuple to catalog", e);
            }

            var tupleNodes = redisReader.getTupleNodes(randTupleId);
            assertEquals(1, tupleNodes.size());
            assertEquals(randNodeId, tupleNodes.toArray()[0]);

            var nodeUnassignedTuples = redisReader.runGetUnassignedTuples(randNodeId);
            assertEquals(1, nodeUnassignedTuples.size());
            assertEquals(randTupleId, nodeUnassignedTuples.toArray()[0]);

            final String randFragmentId = UUID.randomUUID().toString();

            try {
                redisWriter.formFragment(Collections.singleton(randTupleId), randFragmentId, randNodeId);
            } catch (MddeRegistryException e) {
                fail("Failed to form a fragment", e);
            }

            tupleNodes = redisReader.runGetTupleNodes(randTupleId);
            assertEquals(1, tupleNodes.size());
            assertEquals(randNodeId, tupleNodes.toArray()[0]);

            var tupleFragment = redisReader.runGetTupleFragment(randTupleId);
            assertEquals(randFragmentId, tupleFragment);


            try {
                redisWriter.deleteTuple(randTupleId);
            } catch (MddeRegistryException e) {
                fail("Failed to remove a tuple from catalog", e);
            }

            tupleNodes = redisReader.runGetTupleNodes(randNodeId);
            assertEquals(0, tupleNodes.size());
        }
        finally {
            // Cleanup
            var redisConnectionHelper = RedisConnectionHelper.getInstance();
            try(var redisCommand = redisConnectionHelper.getRedisCommands()) {
                redisCommand.del(Constants.NODES_SET);
                redisCommand.del(Constants.NODE_PREFIX + randNodeId);
                redisCommand.del(Constants.NODE_HEAP + randNodeId);
            }
        }
    }

    @Test
    public void testMultiTupleLifecycle(){
        var testConfig = new ConfigRedis();
        var serializer = new ResponseSerializerPassThrough();

        if(!RedisConnectionHelper.getInstance().getIsInitialized()){
            RedisConnectionHelper.getInstance().initialize(testConfig);
        }

        var redisReader = new ReadCommandHandlerRedis<Object>(serializer, testConfig);
        var redisWriter = new WriteCommandHandlerRedis<Object>(redisReader, serializer, testConfig);

        final int tuplesCountPerNode = 100;
        final int nodesCount = 10;
        final int fragmentSize = 25;
        // Node, tuples
        Map<String, Set<String>> nodes = new HashMap<>();
        // Node, fragments
        Map<String, Set<String>> nodesToFragments = new HashMap<>();
        // Fragment, tuples
        Map<String, Set<String>> fragments = new HashMap<>();

        // Generate Nodes
        for(int i = 0; i < nodesCount; i++){
            var nodeTuples = new HashSet<String>();
            nodes.put("Node_" + UUID.randomUUID().toString(), nodeTuples);

            for(int j = 0; j < tuplesCountPerNode; j ++){
                nodeTuples.add("Tuple_" + UUID.randomUUID().toString());
            }
        }
        try {
            // Populate registry nodes
            try {
                redisWriter.populateNodes(nodes.keySet());
            } catch (MddeRegistryException e) {
                fail("Failed to populate nodes", e);
            }
            // Check that the test set and the populated registry nodes are the same
            var populatedNodes = redisReader.runGetNodes();
            assertEquals(nodes.keySet(), populatedNodes);
            // Populate tuples
            for(Map.Entry<String, Set<String>> node: nodes.entrySet()){
                try {
                    redisWriter.insertTuple(node.getValue(), node.getKey());
                } catch (MddeRegistryException e) {
                    fail(String.format("Failed to populate tuples for node %s", node.getKey()), e);
                }
                // Validate insertions
                var populatedUnassignedTuples = redisReader.runGetUnassignedTuples(node.getKey());
                assertEquals(node.getValue(), populatedUnassignedTuples);
            }

            // Create fragments
            for(Map.Entry<String, Set<String>> node: nodes.entrySet()){
                Set<String> nodeFragments = new HashSet<>();
                nodesToFragments.put(node.getKey(), nodeFragments);
                String[] availableTuplesList = redisReader.runGetUnassignedTuples(node.getKey()).toArray(new String[0]);
                // Generate fragments for node
                int currentStart = 0;
                while(currentStart < availableTuplesList.length){
                    var nextStart =  Math.max(currentStart + fragmentSize, availableTuplesList.length);
                    var tupleSubRangeArray = Arrays.copyOfRange(availableTuplesList, currentStart, nextStart);
                    Set<String> tupleSubRangeSet = new HashSet<>(Arrays.asList(tupleSubRangeArray));
                    var randFragmentId = "Fragment_" + UUID.randomUUID().toString();
                    fragments.put(randFragmentId, tupleSubRangeSet);
                    try {
                        redisWriter.formFragment(tupleSubRangeSet, randFragmentId, node.getKey());
                    } catch (MddeRegistryException e) {
                        fail("Failed to form a fragment", e);
                    }
                    // Validate input
                    Set<String> currentFragmentTuplesInRedis = null;
                    try {
                        currentFragmentTuplesInRedis = redisReader.runGetFragmentTuples(randFragmentId);
                    } catch (ReadOperationException e) {
                        fail(String.format("Failed to to read tuples for fragment %s", randFragmentId), e);
                    }
                    assertEquals(tupleSubRangeSet, currentFragmentTuplesInRedis);
                    var fragmentNodes = redisReader.runGetFragmentNodes(randFragmentId);
                    assertTrue(fragmentNodes.contains(node.getKey()));

                    nodeFragments.add(randFragmentId);
                    currentStart = nextStart;
                }

                var storedNodeFragments = redisReader.runGetNodeFragments(node.getKey());
                assertEquals(nodeFragments, storedNodeFragments);
            }

            var nodesArray = nodes.keySet().toArray(new String[0]);
            var fragmentsArray = fragments.keySet().toArray(new String[0]);

            // Shuffle nodes around and validate
            // Validate constraints
            // -- no local replication
            var nlrNodeA= nodesArray[0];
            var nlrNodeB = nodesArray[1];
            var nlrFragment = nodesToFragments.get(nlrNodeA).iterator().next();
            assertThrows(IllegalRegistryActionException.class,
                    () -> redisWriter.replicateFragment(nlrFragment, nlrNodeA, nlrNodeA));
            assertThrows(UnknownEntityIdException.class,
                    () -> redisWriter.replicateFragment(nlrFragment, "foo", nlrNodeA));
            assertThrows(UnknownEntityIdException.class,
                    () -> redisWriter.replicateFragment(nlrFragment, nlrNodeA, "foo"));
            assertThrows(UnknownEntityIdException.class,
                    () -> redisWriter.replicateFragment("foo", nlrNodeA, nlrNodeA));

            try {
                redisWriter.replicateFragment(nlrFragment, nlrNodeA, nlrNodeB);
            } catch (MddeRegistryException e) {
                fail(String.format("Failed to replicate fragment %s from node %s to node %s", nlrFragment, nlrNodeA, nlrNodeB), e);
            }

            var nlrDuplicateExceptionThrown = false;
            try {
                redisWriter.replicateFragment(nlrFragment, nlrNodeA, nlrNodeB);
            } catch (IllegalRegistryActionException e){
                assertEquals(IllegalRegistryActionException.IllegalActions.DuplicateFragmentReplication, e.getAction());
                nlrDuplicateExceptionThrown = true;
            }
            catch (MddeRegistryException e) {
                fail("Incorrect exception thrown while checking anti-duplicate replication constrain", e);
            }
            assertTrue(nlrDuplicateExceptionThrown);

            nodesToFragments.get(nlrNodeB).add(nlrFragment);
            var testNodeATupleSet = nodes.get(nlrNodeA);
            var testNodeBTupleSet = nodes.get(nlrNodeB);
            testNodeBTupleSet.addAll(fragments.get(nlrFragment));

            var nlrFragmentNodesTest = new HashSet<String>();
            nlrFragmentNodesTest.add(nlrNodeA);
            nlrFragmentNodesTest.add(nlrNodeB);

            var nlrFragmentNodesStored = redisReader.runGetFragmentNodes(nlrFragment);
            assertEquals(nlrFragmentNodesTest, nlrFragmentNodesStored);

            /*
            final int shuffleIterations = 100;
            Random rand = new Random();

            for(int i = 0; i < shuffleIterations; i++){
                var randomNodeA = nodesArray[rand.nextInt(nodesArray.length)];
                var randomNodeB = nodesArray[rand.nextInt(nodesArray.length)];

                var nodeAFragmentsStored = redisReader.getNodeFragments(randomNodeA);
                assertEquals(nodesToFragments.get(randomNodeA), nodeAFragmentsStored);

            }

             */
        } finally {
            // Cleanup
            var redisConnectionHelper = RedisConnectionHelper.getInstance();
            try(var jedis = redisConnectionHelper.getRedisCommands()) {
                jedis.del(Constants.NODES_SET);
                try (var p = jedis.pipelined()) {
                    // Fragments
                    p.del(Constants.FRAGMENT_SET);
                    for (Map.Entry<String, Set<String>> fragment : fragments.entrySet()) {
                        p.del(Constants.FRAGMENT_PREFIX + fragment.getKey());
                    }
                    // Nodes
                    for (Map.Entry<String, Set<String>> node : nodes.entrySet()) {
                        p.del(Constants.NODE_PREFIX + node.getKey());
                        p.del(Constants.NODE_HEAP + node.getKey());
                    }
                    p.sync();
                }
            }
        }
    }
}
