package dev.jcri.mdde.registry.exceptions;

import java.util.Objects;

/**
 * Base class for the hierarchy of the custom MDDE-Registry exceptions
 */
public abstract class MddeRegistryException extends Exception {

    protected final EErrorCode errorCode;

    public MddeRegistryException(EErrorCode errorCode, String message){
        this(errorCode, message, null);
    }

    public MddeRegistryException(EErrorCode errorCode, Throwable cause){
        this(errorCode, null, cause);
    }

    public MddeRegistryException(EErrorCode errorCode, String message, Throwable cause){
        super(message, cause);

        Objects.requireNonNull(errorCode, "MDDE exceptions must be supplied with a unique error code");
        this.errorCode = errorCode;
    }

    /**
     * Error code associated with the exception. Must be always send to the client. Can never be null.
     * @return EErrorCode value
     */
    public EErrorCode getErrorCode(){
        return errorCode;
    }
}
