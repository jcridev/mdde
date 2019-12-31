package dev.jcri.mdde.registry.control.serialization;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
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

    @Override
    public String serializeException(Throwable cause) {
        try {
            return _mapper.writeValueAsString(new ResultJsonContainer<String>(null, cause.getMessage()));
        } catch (JsonProcessingException e) {
            String error = e.getMessage();
            if(cause != null && cause.getMessage() != null){
                error = String.format("%s | %s", error, cause.getMessage());
            }
            error = error.replaceAll("\"", "'");
            return String.format("{\"error\": \"%s\"}", error);
        }
    }

    private static final class ResultJsonContainer<T> {
        private T _result;
        private String _error;

        public ResultJsonContainer(){}

        public ResultJsonContainer(T result, String error){
            _result = result;
            _error = error;
        }
        @JsonGetter("result")
        public T getResult() {
            return _result;
        }
        @JsonSetter("result")
        public void setResult(T result) {
            this._result = result;
        }
        @JsonGetter("error")
        public String getError() {
            return _error;
        }
        @JsonSetter("error")
        public void setError(String error) {
            this._error = error;
        }
    }
}
