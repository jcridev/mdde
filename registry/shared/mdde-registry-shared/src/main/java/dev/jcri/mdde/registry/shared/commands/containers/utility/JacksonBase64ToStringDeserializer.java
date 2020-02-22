package dev.jcri.mdde.registry.shared.commands.containers.utility;

import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Base64;

/**
 * Allows us to retrieve an internal node of a json as a string for later custom deserialization flow
 */
public class JacksonBase64ToStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        String encodedString = jp.getCodec().readTree(jp).toString();
        if(encodedString != null){
            encodedString = encodedString.replaceAll("^\"+", "").replaceAll("\"+$", "");
            return new String(Base64.getDecoder().decode(encodedString));
        }
        return null;
    }
}