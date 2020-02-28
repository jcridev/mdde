package dev.jcri.mdde.registry.shared.commands.containers;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerJson;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistryAllocation;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestCommandResponseSerialization {

    @Test
    public void testList(){
        var expectedResult = new ArrayList<String>();
        expectedResult.add("First");
        expectedResult.add("Second");
        expectedResult.add("Third");
        ResponseSerializerJson jsonSerializer = new ResponseSerializerJson();
        String response = null;
        try {
            response = jsonSerializer.serialize(expectedResult);
        } catch (ResponseSerializationException e) {
            fail(e.getMessage());
        }

        CommandResultContainer<ArrayList<String>> deserializedJSON = null;
        try {
            deserializedJSON = CommandSerializationHelper.deserializeJson(response);
        } catch (JsonProcessingException e) {
            fail(e.getMessage());
        }

        assertNotNull(deserializedJSON);
        assertEquals(expectedResult, deserializedJSON.getResult());
    }

    @Test
    public void  testString(){
        var expectedResult = "ok";
        ResponseSerializerJson jsonSerializer = new ResponseSerializerJson();
        String response = null;
        try {
            response = jsonSerializer.serialize(expectedResult);
        } catch (ResponseSerializationException e) {
            fail(e.getMessage());
        }

        CommandResultContainer<String> deserializedJSON = null;
        try {
            deserializedJSON = CommandSerializationHelper.<String>deserializeJson(response);
        } catch (JsonProcessingException e) {
            fail(e.getMessage());
        }

        assertNotNull(deserializedJSON);
        assertEquals(expectedResult, deserializedJSON.getResult());
    }

    @Test
    public void testInt(){
        var expectedResult = 123456789;
        ResponseSerializerJson jsonSerializer = new ResponseSerializerJson();
        String response = null;
        try {
            response = jsonSerializer.serialize(expectedResult);
        } catch (ResponseSerializationException e) {
            fail(e.getMessage());
        }

        CommandResultContainer<Integer> deserializedJSON = null;
        try {
            deserializedJSON = CommandSerializationHelper.deserializeJson(response);
        } catch (JsonProcessingException e) {
            fail(e.getMessage());
        }

        assertNotNull(deserializedJSON);
        assertEquals(expectedResult, deserializedJSON.getResult());
    }

    @Test
    public void testFullRegistry(){
        Map<String, Map<String, Set<String>>> items = new HashMap<String, Map<String, Set<String>>>();
        var firstNodeFragments = new HashMap<String, Set<String>>();
        firstNodeFragments.put("Fragment ID 1", new HashSet<>(Arrays.asList("Item 1", "Item 2")));
        firstNodeFragments.put("Fragment ID 2", new HashSet<>(Arrays.asList("Item 3", "Item 4")));
        items.put("Node ID 1",firstNodeFragments);

        var secondNodeFragments = new HashMap<String, Set<String>>();
        secondNodeFragments.put("Fragment ID 2", new HashSet<>(Arrays.asList("Item 3", "Item 4")));
        secondNodeFragments.put("Fragment ID 3", new HashSet<>(Arrays.asList("Item 5", "Item 6")));
        items.put("Node ID 2",secondNodeFragments);

        var expectedResult = new FullRegistryAllocation(items);

        ResponseSerializerJson jsonSerializer = new ResponseSerializerJson();
        String response = null;
        try {
            response = jsonSerializer.serialize(expectedResult);
        } catch (ResponseSerializationException e) {
            fail(e.getMessage());
        }

        CommandResultContainer<FullRegistryAllocation> deserializedJSON = null;
        try {
            deserializedJSON = CommandSerializationHelper.<FullRegistryAllocation>deserializeJson(FullRegistryAllocation.class, response);
        } catch (JsonProcessingException e) {
            fail(e.getMessage());
        }
        assertNotNull(deserializedJSON);


        FullRegistryAllocation retrievedRegistry = deserializedJSON.getResult();
        assertEquals(expectedResult, retrievedRegistry);
    }
}
