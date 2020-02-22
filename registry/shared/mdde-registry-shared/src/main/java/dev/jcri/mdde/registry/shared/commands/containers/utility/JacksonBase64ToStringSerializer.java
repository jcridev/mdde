package dev.jcri.mdde.registry.shared.commands.containers.utility;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;

public class JacksonBase64ToStringSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String str, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(str != null) {
            String bs64String = Base64.getEncoder().encodeToString(str.getBytes());
            jsonGenerator.writeString(bs64String);
        }
        else{
            jsonGenerator.writeNull();
        }
    }
}
