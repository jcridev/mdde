package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class ReadOperationException extends MddeRegistryException {
    public ReadOperationException(String message){
        super(message);
    }

    public ReadOperationException(Throwable cause){
        super(cause);
    }
}
