package dev.jcri.mdde.registry.control.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jcri.mdde.registry.shared.commands.Constants;
import dev.jcri.mdde.registry.shared.commands.containers.CommandResultContainer;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistry;

import java.util.List;
import java.util.Set;

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
            return _mapper.writeValueAsString(new CommandResultContainer<String>(value, null));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(List<String> value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(
                    new CommandResultContainer<List<String>>(value, null)
            );
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(Set<String> value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(
                    new CommandResultContainer<Set<String>>(value, null)
            );
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(int value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<Integer>(value, null));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(boolean value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(
                    new CommandResultContainer<Boolean>(value, null)
            );
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(FullRegistry value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<FullRegistry>(value, null));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(BenchmarkRunResult value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<BenchmarkRunResult>(value, null));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(BenchmarkStatus value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<BenchmarkStatus>(value, null));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(FragmentCatalog value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<FragmentCatalog>(value, null));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serializeException(Throwable cause) {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<String>(null, cause.getMessage()));
        } catch (JsonProcessingException e) {
            return getSerializationError(cause, e);
        }
    }

    private String getSerializationError(Throwable cause, JsonProcessingException e) {
        String error = e.getMessage();
        if (cause != null && cause.getMessage() != null) {
            error = String.format("%s | %s", error, cause.getMessage());
        }
        error = error.replaceAll("\"", "'");
        return String.format("{\"%s\": null, \"%s\": \"%s\"}", Constants.ResultPayload, Constants.ResultError, error);
    }
}
