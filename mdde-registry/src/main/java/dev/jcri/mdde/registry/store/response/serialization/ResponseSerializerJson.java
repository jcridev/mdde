package dev.jcri.mdde.registry.store.response.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.store.response.FullRegistry;

import java.util.List;

/**
 * Serialize responses to raw JSON String
 */
public class ResponseSerializerJson implements IResponseSerializer<String> {
    private final ObjectMapper _mapper;

    public ResponseSerializerJson(){
        _mapper  = new ObjectMapper();
    }

    @Override
    public String serialize(String value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(List<String> value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(int value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(Object value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(FullRegistry value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }
}
