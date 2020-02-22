package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Thrown when a serialization error occurred
 */
public class ResponseSerializationException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.RESPONSE_SERIALIZATION_ERROR;

    public ResponseSerializationException(String message, Throwable cause){
        super(_exCode, message, cause);
    }

    public ResponseSerializationException(String message){
        this(message, null);
    }

    public ResponseSerializationException(Throwable cause){
        this(null, cause);
    }
}
