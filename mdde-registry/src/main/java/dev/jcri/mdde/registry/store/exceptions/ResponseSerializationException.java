package dev.jcri.mdde.registry.store.exceptions;

/**
 * Thrown when a serialization error occurred
 */
public class ResponseSerializationException extends Exception {
    public ResponseSerializationException(String message){
        super(message);
    }

    public ResponseSerializationException(Throwable cause){
        super(cause);
    }
}
