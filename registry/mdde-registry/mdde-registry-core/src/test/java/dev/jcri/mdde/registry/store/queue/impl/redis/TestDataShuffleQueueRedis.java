package dev.jcri.mdde.registry.store.queue.impl.redis;

import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;
import dev.jcri.mdde.registry.store.impl.redis.RedisConnectionHelper;
import dev.jcri.mdde.registry.store.queue.IDataShuffleQueue;
import dev.jcri.mdde.registry.store.queue.actions.DataAction;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;
import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class TestDataShuffleQueueRedis {

    @Container
    public static GenericContainer redis = new GenericContainer<>("redis:5").withExposedPorts(6379);

    public final static String TEST_QUEUE_LIST_KEY = "test_mdde/queue";
    public static RedisConnectionHelper redisRegistryStoreConnHelper;

    @BeforeAll
    public static void setUpRedis() {
        RegistryStoreConfigRedis redisStoreConfig = new RegistryStoreConfigRedis();
        redisStoreConfig.setHost(redis.getContainerIpAddress());
        redisStoreConfig.setPort(redis.getFirstMappedPort());

        redisRegistryStoreConnHelper = new RedisConnectionHelper(redisStoreConfig);
    }

    @AfterAll
    public static void cleanup(){
        if(redisRegistryStoreConnHelper != null){
            redisRegistryStoreConnHelper.close();
        }
    }

    @Test
    public void TestQueueAddPop(){
        final String nodeId1 = UUID.randomUUID().toString();
        final String nodeId2 = UUID.randomUUID().toString();
        final String nodeId3 = UUID.randomUUID().toString();

        List<DataAction> testActions = new ArrayList<>();
        DataCopyAction copy1 = new DataCopyAction(generateSetOfUUIDs(100), nodeId1, nodeId2);
        DataCopyAction copy2 = new DataCopyAction(generateSetOfUUIDs(200), nodeId2, nodeId3);
        DataCopyAction copy3 = new DataCopyAction(generateSetOfUUIDs(100), nodeId2, nodeId1);
        DataDeleteAction del1 = new DataDeleteAction(generateSetOfUUIDs(100), nodeId1);
        DataDeleteAction del2 = new DataDeleteAction(generateSetOfUUIDs(300), nodeId2);
        DataDeleteAction del3 = new DataDeleteAction(generateSetOfUUIDs(400), nodeId3);

        testActions.add(copy1);
        testActions.add(del1);
        testActions.add(del2);
        testActions.add(copy2);
        testActions.add(copy3);
        testActions.add(del3);

        IDataShuffleQueue queue = new DataShuffleQueueRedis(redisRegistryStoreConnHelper, TEST_QUEUE_LIST_KEY);

        for(var testItem: testActions){
            try {
                queue.add(testItem);
            } catch (IOException e) {
                fail(e);
            }
        }
        // Should get the items back in the same order they were inserted
        for(var testItem: testActions) {
            try {
                DataAction action = queue.poll();
                assertEquals(testItem.getActionId(), action.getActionId());
                assertEquals(action.getActionType(), testItem.getActionType());

                switch (action.getActionType()){
                    case DELETE:
                        DataDeleteAction castedDelPolled = (DataDeleteAction) action;
                        DataDeleteAction castedDelTestItem = (DataDeleteAction)testItem;
                        assertEquals(castedDelTestItem.getTupleIds(), castedDelPolled.getTupleIds());
                        assertEquals(castedDelTestItem.getDataNode(), castedDelPolled.getDataNode());
                        break;
                    case COPY:
                        DataCopyAction castedCpyPolled = (DataCopyAction) action;
                        DataCopyAction castedDpyTestItem = (DataCopyAction)testItem;
                        assertEquals(castedCpyPolled.getTupleIds(), castedDpyTestItem.getTupleIds());
                        assertEquals(castedCpyPolled.getSourceNode(), castedDpyTestItem.getSourceNode());
                        assertEquals(castedCpyPolled.getDestinationNode(), castedDpyTestItem.getDestinationNode());
                        break;
                    default:
                        fail("Unknown data action type");
                }

            } catch (IOException e) {
                fail(e);
            }
        }

    }

    public static Set<String> generateSetOfUUIDs(int length){
        Set<String> result = new HashSet<>();
        for(int i = 0; i < length; i++){
            result.add(UUID.randomUUID().toString());
        }
        return result;
    }
}
