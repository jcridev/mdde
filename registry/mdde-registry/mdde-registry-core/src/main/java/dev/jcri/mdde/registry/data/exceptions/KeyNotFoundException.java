package dev.jcri.mdde.registry.data.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class KeyNotFoundException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.DATA_KEY_NOT_FOUND;

    public KeyNotFoundException(String message) {
        this(message, null);
    }

    public KeyNotFoundException(Throwable cause) {
        this(null, cause);
    }

    public KeyNotFoundException(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
