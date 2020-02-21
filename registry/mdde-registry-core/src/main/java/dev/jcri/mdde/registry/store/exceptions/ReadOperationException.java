package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class ReadOperationException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.READ_OPERATION_ERROR;

    public ReadOperationException(String message, Throwable cause){
        super(_exCode, message, cause);
    }

    public ReadOperationException(String message){
        this(message, null);
    }

    public ReadOperationException(Throwable cause){
        this(null, cause);
    }
}
