package dev.jcri.mdde.registry.exceptions;

/**
 * Base class for the hierarchy of the custom MDDE-Registry exceptions
 */
public abstract class MddeRegistryException extends Exception {
    public MddeRegistryException(String message){
        super(message);
    }

    public MddeRegistryException(Throwable cause){
        super(cause);
    }

    public MddeRegistryException(String message, Throwable cause){
        super(message, cause);
    }
}
