package dev.jcri.mdde.registry.control.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * A command received an illegal argument
 */
public class IllegalCommandArgumentException extends CommandException {
    private final static EErrorCode _exCode = EErrorCode.DATA_KEY_NOT_FOUND;

    public IllegalCommandArgumentException(String message) {
        super(_exCode, message);
    }

    public IllegalCommandArgumentException(Throwable cause) {
        super(_exCode, cause);
    }
}
