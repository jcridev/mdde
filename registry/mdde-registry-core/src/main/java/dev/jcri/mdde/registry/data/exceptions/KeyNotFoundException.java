package dev.jcri.mdde.registry.data.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class KeyNotFoundException extends MddeRegistryException {
    public KeyNotFoundException(String message) {
        super(message);
    }

    public KeyNotFoundException(Throwable cause) {
        super(cause);
    }

    public KeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
