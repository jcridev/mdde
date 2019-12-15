package dev.jcri.mdde.registry.store.exceptions;

public class WriteOperationException extends Exception {
    public WriteOperationException(String message){
        super(message);
    }

    public WriteOperationException(Throwable cause){
        super(cause);
    }
}
