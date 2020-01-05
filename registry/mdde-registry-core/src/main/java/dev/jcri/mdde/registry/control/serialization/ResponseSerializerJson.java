package dev.jcri.mdde.registry.control.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jcri.mdde.registry.shared.commands.Constants;
import dev.jcri.mdde.registry.shared.commands.containers.CommandResultContainer;
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

    @Override
    public String serializeException(Throwable cause) {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<String>(null, cause.getMessage()));
        } catch (JsonProcessingException e) {
            String error = e.getMessage();
            if(cause != null && cause.getMessage() != null){
                error = String.format("%s | %s", error, cause.getMessage());
            }
            error = error.replaceAll("\"", "'");
            return String.format("{\"%s\": null, \"%s\": \"%s\"}", Constants.ResultPayload, Constants.ResultError, error);
        }
    }
}
