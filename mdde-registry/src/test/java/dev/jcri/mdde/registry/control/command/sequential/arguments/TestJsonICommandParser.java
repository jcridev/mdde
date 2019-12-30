package dev.jcri.mdde.registry.control.command.sequential.arguments;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestJsonICommandParser {
    @Test
    public void testJsonParsing(){
        String simpeJson = "{\"cmd\":\"r\", \"some\": 1, \"args\":{\"arg1\": 1, \"arg2\": null, \"arg3\": \"test\"," +
                            "\"arg4\": [\"161b1619-9fd5-406b-8a5d-edf18bae7fca\", " +
                                        "\"00a97252-d161-40ed-a900-347e02f10bf8\", " +
                                        "\"305bd88a-38d4-44bf-8cd5-9f3cf02b7eba\", " +
                                        "\"19a1a6ff-d57d-4220-8f4c-b27764f63347\"]}}";

        ObjectMapper mapper = new ObjectMapper();

        //mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

        sampleJsonObj obj = null;
        try {
            obj = mapper.readValue(simpeJson, sampleJsonObj.class);
            assertNotNull(obj);
        } catch (JsonProcessingException e) {
            fail(e);
        }

        JsonNode parent= null;
        try {
            parent = mapper.readTree(obj.args);
            Set<String> content = mapper.convertValue(parent.get("arg4"), HashSet.class);

            assertNotNull(content);
        } catch (JsonProcessingException e) {
            fail(e);
        }

    }

    public static class sampleJsonObj{
        private String cmd;
        @JsonRawValue
        @JsonDeserialize(using = KeepAsJsonDeserialzier.class)
        @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
        private String args;

        private int some;

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public String getArgs() {
            return args;
        }

        public void setArgs(String arguments) {
            this.args = arguments;
        }

        public int getSome() {
            return some;
        }

        public void setSome(int some) {
            this.some = some;
        }
    }
}

class KeepAsJsonDeserialzier extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        TreeNode tree = jp.getCodec().readTree(jp);
        return tree.toString();
    }
}
