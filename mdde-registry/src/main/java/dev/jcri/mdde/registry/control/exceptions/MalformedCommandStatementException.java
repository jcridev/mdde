package dev.jcri.mdde.registry.control.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Incoming command statement was incorrect
 */
public class MalformedCommandStatementException extends MddeRegistryException {
    public MalformedCommandStatementException(String message) {
        super(message);
    }

    public MalformedCommandStatementException(Throwable cause) {
        super(cause);
    }

    public MalformedCommandStatementException(String message, Throwable cause) {
        super(cause);
    }
}
