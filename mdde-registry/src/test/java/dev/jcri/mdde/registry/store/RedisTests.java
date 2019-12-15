package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.exceptions.UnknownEntityIdException;
import dev.jcri.mdde.registry.store.exceptions.WriteOperationException;
import dev.jcri.mdde.registry.store.impl.redis.ConfigRedis;
import dev.jcri.mdde.registry.store.impl.redis.ReadCommandHandlerRedis;

import dev.jcri.mdde.registry.store.impl.redis.Constants;
import dev.jcri.mdde.registry.store.impl.redis.RedisConnectionHelper;
import dev.jcri.mdde.registry.store.impl.redis.WriteCommandHandlerRedis;
import dev.jcri.mdde.registry.store.response.serialization.ResponseSerializerPassThrough;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

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

            var nodeUnassignedTuples = redisReader.getUnassignedTuples(randNodeId);
            assertEquals(1, nodeUnassignedTuples.size());
            assertEquals(randTupleId, nodeUnassignedTuples.toArray()[0]);

            final String randFragmentId = UUID.randomUUID().toString();

            try {
                redisWriter.formFragment(Collections.singleton(randTupleId), randFragmentId, randNodeId);
            } catch (MddeRegistryException e) {
                fail("Failed to form a fragment", e);
            }

            tupleNodes = redisReader.getTupleNodes(randTupleId);
            assertEquals(1, tupleNodes.size());
            assertEquals(randNodeId, tupleNodes.toArray()[0]);

            var tupleFragment = redisReader.getTupleFragment(randTupleId);
            assertEquals(randFragmentId, tupleFragment);


            try {
                redisWriter.deleteTuple(randTupleId);
            } catch (MddeRegistryException e) {
                fail("Failed to remove a tuple from catalog", e);
            }

            tupleNodes = redisReader.getTupleNodes(randNodeId);
            assertEquals(0, tupleNodes.size());
        }
        finally {
            // Cleanup
            var redisConnectionHelper = new RedisConnectionHelper(testConfig);
            var redisCommand = redisConnectionHelper.getRedisCommands();
            redisCommand.del(Constants.NODES_SET);
            redisCommand.del(Constants.NODE_PREFIX + randNodeId);
            redisCommand.del(Constants.NODE_HEAP + randNodeId);
        }
    }
}
