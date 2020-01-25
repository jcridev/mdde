package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Error during the alteration of the registry
 */
public class WriteOperationException extends MddeRegistryException {
    public WriteOperationException(String message){
        super(message);
    }

    public WriteOperationException(Throwable cause){
        super(cause);
    }

    public WriteOperationException(String message, Throwable cause){
        super(message, cause);
    }
}
