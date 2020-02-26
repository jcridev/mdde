package dev.jcri.mdde.registry.store.exceptions.action;

import dev.jcri.mdde.registry.exceptions.EErrorCode;

public class SeedNonEmptyRegistryException extends IllegalRegistryActionException {

    private final static EErrorCode _exCode = EErrorCode.SEED_NON_EMPTY_REGISTRY;

    public SeedNonEmptyRegistryException(String message) {
        this(message, null);
    }

    public SeedNonEmptyRegistryException(Throwable cause) {
        this(null, cause);
    }

    public SeedNonEmptyRegistryException(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
