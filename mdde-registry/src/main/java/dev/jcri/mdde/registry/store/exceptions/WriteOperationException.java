package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class WriteOperationException extends MddeRegistryException {
    public WriteOperationException(String message){
        super(message);
    }

    public WriteOperationException(Throwable cause){
        super(cause);
    }
}
