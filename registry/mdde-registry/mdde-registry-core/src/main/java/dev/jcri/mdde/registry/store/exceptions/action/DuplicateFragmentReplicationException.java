package dev.jcri.mdde.registry.store.exceptions.action;

import dev.jcri.mdde.registry.exceptions.EErrorCode;

public class DuplicateFragmentReplicationException extends IllegalRegistryActionException {
    private final static EErrorCode _exCode = EErrorCode.DUPLICATE_FRAGMENT_REPLICATION;

    public DuplicateFragmentReplicationException(String message) {
        this(message, null);
    }

    public DuplicateFragmentReplicationException(Throwable cause) {
        this(null, cause);
    }

    public DuplicateFragmentReplicationException(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
