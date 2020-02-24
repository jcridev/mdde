package dev.jcri.mdde.registry.control.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public abstract class CommandException extends MddeRegistryException {
    public CommandException(EErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CommandException(EErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public CommandException(EErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
