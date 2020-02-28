package dev.jcri.mdde.registry.control.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.shared.commands.Constants;
import dev.jcri.mdde.registry.shared.commands.containers.CommandResultContainer;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistryAllocation;

import java.util.List;
import java.util.Set;

/**
 * Serialize responses to raw JSON String
 */
public class ResponseSerializerJson extends ResponseSerializerBase<String> {
    private final ObjectMapper _mapper;

    public ResponseSerializerJson(){
        _mapper  = new ObjectMapper();
    }

    @Override
    public String serialize(String value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<String>(value));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(List<String> value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(
                    new CommandResultContainer<List<String>>(value)
            );
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(Set<String> value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(
                    new CommandResultContainer<Set<String>>(value)
            );
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(int value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<Integer>(value));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(boolean value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(
                    new CommandResultContainer<Boolean>(value)
            );
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(FullRegistryAllocation value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<FullRegistryAllocation>(value));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(BenchmarkRunResult value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<BenchmarkRunResult>(value));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(BenchmarkStatus value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<BenchmarkStatus>(value));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    public String serialize(FragmentCatalog value) throws ResponseSerializationException {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<FragmentCatalog>(value));
        } catch (JsonProcessingException e) {
            throw new ResponseSerializationException(e);
        }
    }

    @Override
    protected String serializeErrorWithCode(EErrorCode errorCode, String message) {
        try {
            return _mapper.writeValueAsString(new CommandResultContainer<String>(null, message, errorCode));
        } catch (JsonProcessingException e) {
            return getSerializationError(message, e);
        }
    }

    /**
     * Error serializing an error (for example if Jackson fails for some reason)
     * @param causeMessage Optional original error message
     * @param e Cause
     * @return Serialization error string
     */
    private String getSerializationError(String causeMessage, JsonProcessingException e) {
        String error = e.getMessage();
        if (causeMessage != null) {
            error = String.format("%s | %s", error, causeMessage);
        }
        error = error.replaceAll("\"", "'");

        return java.text.MessageFormat.format("{\"{0}\": null, \"{1}\": \"{2}\",\"{3}\": {4}}",
                Constants.ResultPayload,
                Constants.ResultError,  error,
                Constants.ResultErrorCode,  EErrorCode.RESPONSE_SERIALIZATION_ERROR.getErrorCode());
    }
}
