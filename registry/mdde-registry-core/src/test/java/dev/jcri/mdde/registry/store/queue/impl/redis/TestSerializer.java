package dev.jcri.mdde.registry.store.queue.impl.redis;

import dev.jcri.mdde.registry.store.queue.actions.DataAction;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;
import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TestSerializer {

    @Test
    public void testDataCopyActionSerializationDeserialization(){
        String tId0 = UUID.randomUUID().toString();
        String tId1 = UUID.randomUUID().toString();
        String tId2 = UUID.randomUUID().toString();
        String tId3 = UUID.randomUUID().toString();
        String tId4 = UUID.randomUUID().toString();

        String sourceNodeId = UUID.randomUUID().toString();
        String destinationNodeId = UUID.randomUUID().toString();

        Set<String> tupleIds = new HashSet<>();
        tupleIds.add(tId0);
        tupleIds.add(tId1);
        tupleIds.add(tId2);
        tupleIds.add(tId3);
        tupleIds.add(tId4);

        DataCopyAction value = new DataCopyAction(tupleIds, sourceNodeId, destinationNodeId);

        String serialized = null;
        try {
            serialized = Serializer.serialize(value);
        } catch (IOException e) {
            fail(e);
        }
        assertNotNull(serialized);

        DataAction deserialized = null;
        try {
            deserialized = Serializer.deserialize(serialized);
        } catch (IOException e) {
            fail(e);
        }
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof DataCopyAction);

        DataCopyAction casted = (DataCopyAction)deserialized;
        assertEquals(tupleIds, casted.getTupleIds());
        assertEquals(sourceNodeId, casted.getSourceNode());
        assertEquals(destinationNodeId, casted.getDestinationNode());
        assertEquals(value.getActionType(), casted.getActionType());
        assertEquals(value.getActionType().getCode(), casted.getActionType().getCode());
    }

    @Test
    public void testDataDeleteActionSerializationDeserialization(){
        String tId0 = UUID.randomUUID().toString();
        String tId1 = UUID.randomUUID().toString();
        String tId2 = UUID.randomUUID().toString();
        String tId3 = UUID.randomUUID().toString();
        String tId4 = UUID.randomUUID().toString();

        String dataNodeId = UUID.randomUUID().toString();

        Set<String> tupleIds = new HashSet<>();
        tupleIds.add(tId0);
        tupleIds.add(tId1);
        tupleIds.add(tId2);
        tupleIds.add(tId3);
        tupleIds.add(tId4);

        DataDeleteAction value = new DataDeleteAction(tupleIds, dataNodeId);

        String serialized = null;
        try {
            serialized = Serializer.serialize(value);
        } catch (IOException e) {
            fail(e);
        }
        assertNotNull(serialized);

        DataAction deserialized = null;
        try {
            deserialized = Serializer.deserialize(serialized);
        } catch (IOException e) {
            fail(e);
        }
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof DataDeleteAction);

        DataDeleteAction casted = (DataDeleteAction)deserialized;
        assertEquals(tupleIds, casted.getTupleIds());
        assertEquals(dataNodeId, casted.getDataNode());
        assertEquals(value.getActionType(), casted.getActionType());
        assertEquals(value.getActionType().getCode(), casted.getActionType().getCode());
    }
}
