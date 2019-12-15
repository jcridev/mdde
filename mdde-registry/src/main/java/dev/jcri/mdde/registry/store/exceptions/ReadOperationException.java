package dev.jcri.mdde.registry.store.exceptions;

public class ReadOperationException extends Exception {
    public ReadOperationException(String message){
        super(message);
    }

    public ReadOperationException(Throwable cause){
        super(cause);
    }
}
