package dev.jcri.mdde.registry.control.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * A command received an illegal argument
 */
public class IllegalCommandArgumentException extends MddeRegistryException {

    public IllegalCommandArgumentException(String message) {
        super(message);
    }

    public IllegalCommandArgumentException(Throwable cause) {
        super(cause);
    }
}
