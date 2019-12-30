package dev.jcri.mdde.registry.control.command.json.extensions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Allows us to retrieve an internal node of a json as a string for later custom deserialization flow
 */
public class JacksonKeepAsStringDeserializer  extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        TreeNode tree = jp.getCodec().readTree(jp);
        return tree.toString();
    }
}