package dev.jcri.mdde.registry.control.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Incoming command statement was incorrect
 */
public class MalformedCommandStatementException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.CTRL_MALFORMED_COMMAND_STATEMENT;

    public MalformedCommandStatementException(String message) {
        this(message, null);
    }

    public MalformedCommandStatementException(Throwable cause) {
        this(null, cause);
    }

    public MalformedCommandStatementException(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
