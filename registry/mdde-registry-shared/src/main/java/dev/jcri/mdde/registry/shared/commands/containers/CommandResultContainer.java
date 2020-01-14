package dev.jcri.mdde.registry.shared.commands.containers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.jcri.mdde.registry.shared.commands.Constants;
import dev.jcri.mdde.registry.shared.commands.containers.utility.JacksonBase64ToStringSerializer;

public class CommandResultContainer<T> {
    private T _result;
    private String _error;

    public CommandResultContainer(){}

    public CommandResultContainer(T result, String error){
        _result = result;
        _error = error;
    }
    @JsonGetter(Constants.ResultPayload)
    public T getResult() {
        return _result;
    }
    @JsonSetter(Constants.ResultPayload)
    public void setResult(T result) {
        this._result = result;
    }
    @JsonGetter(Constants.ResultError)
    public String getError() {
        return _error;
    }
    @JsonSetter(Constants.ResultError)
    public void setError(String error) {
        this._error = error;
    }
}
