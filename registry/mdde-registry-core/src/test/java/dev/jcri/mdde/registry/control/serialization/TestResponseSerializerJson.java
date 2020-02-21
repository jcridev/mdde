package dev.jcri.mdde.registry.control.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestResponseSerializerJson {

    @Test
    public void testExceptionSerialization(){
        var testException = new Exception("this is a test exception message");
        var expectedResult = "{\"error\":\"this is a test exception message\",\"result\":null}";
        ResponseSerializerBase<String> jsonSerializer = new ResponseSerializerJson();
        var response = jsonSerializer.serializeException(testException);

        assertEquals(expectedResult, response);
    }

}
