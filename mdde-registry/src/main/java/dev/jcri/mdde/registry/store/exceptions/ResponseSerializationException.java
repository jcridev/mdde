package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Thrown when a serialization error occurred
 */
public class ResponseSerializationException extends MddeRegistryException {
    public ResponseSerializationException(String message){
        super(message);
    }

    public ResponseSerializationException(Throwable cause){
        super(cause);
    }
}
