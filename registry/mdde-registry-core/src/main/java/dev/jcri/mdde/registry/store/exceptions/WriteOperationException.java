package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Error during the alteration of the registry
 */
public class WriteOperationException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.WRITE_OPERATION_ERROR;

    public WriteOperationException(String message){
        this(message, null);
    }

    public WriteOperationException(Throwable cause){
        this(null, cause);
    }

    public WriteOperationException(String message, Throwable cause){
        super(_exCode, message, cause);
    }
}
