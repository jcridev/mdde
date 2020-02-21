package dev.jcri.mdde.registry.shared.commands.containers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.shared.commands.Constants;
import dev.jcri.mdde.registry.shared.commands.containers.utility.JacksonBase64ToStringSerializer;

public class CommandResultContainer<T> {
    private T _result;
    private String _error;
    private EErrorCode _errorCode;

    public CommandResultContainer(){}

    public CommandResultContainer(T result){
        this(result, null, null);
    }

    public CommandResultContainer(T result, String error, EErrorCode errorCode){
        _result = result;
        _error = error;
        _errorCode = errorCode;
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

    @JsonGetter(Constants.ResultErrorCode)
    public Integer getErrorCode() {
        if (_errorCode == null){
            return null;
        }
        return _errorCode.getErrorCode();
    }
    @JsonSetter(Constants.ResultErrorCode)
    public void setErrorCode(Integer errorCode) {
        this._errorCode = EErrorCode.getValue(errorCode);
    }
}
