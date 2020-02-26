package dev.jcri.mdde.registry.store.exceptions.action;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Thrown when there is an attempt to perform an action that is explicitly prohibited by the registry
 */
public abstract class IllegalRegistryActionException extends MddeRegistryException {

    public IllegalRegistryActionException(EErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public IllegalRegistryActionException(EErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public IllegalRegistryActionException(EErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
