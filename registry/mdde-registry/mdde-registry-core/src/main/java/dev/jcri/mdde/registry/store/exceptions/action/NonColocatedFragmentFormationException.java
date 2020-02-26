package dev.jcri.mdde.registry.store.exceptions.action;

import dev.jcri.mdde.registry.exceptions.EErrorCode;

public class NonColocatedFragmentFormationException extends IllegalRegistryActionException {
    private final static EErrorCode _exCode = EErrorCode.NON_COLOCATED_FRAGMENT_FORMATION;

    public NonColocatedFragmentFormationException(String message) {
        this(message, null);
    }

    public NonColocatedFragmentFormationException(Throwable cause) {
        this(null, cause);
    }

    public NonColocatedFragmentFormationException(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
