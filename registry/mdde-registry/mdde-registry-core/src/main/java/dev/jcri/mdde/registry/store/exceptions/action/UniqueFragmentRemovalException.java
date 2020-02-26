package dev.jcri.mdde.registry.store.exceptions.action;

import dev.jcri.mdde.registry.exceptions.EErrorCode;

public class UniqueFragmentRemovalException extends IllegalRegistryActionException {

    private final static EErrorCode _exCode = EErrorCode.UNIQUE_FRAGMENT_REMOVAL;

    public UniqueFragmentRemovalException(String message) {
        this(message, null);
    }

    public UniqueFragmentRemovalException(Throwable cause) {
        this(null, cause);
    }

    public UniqueFragmentRemovalException(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
