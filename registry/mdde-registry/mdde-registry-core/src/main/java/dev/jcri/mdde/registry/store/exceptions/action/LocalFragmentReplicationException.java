package dev.jcri.mdde.registry.store.exceptions.action;

import dev.jcri.mdde.registry.exceptions.EErrorCode;

public class LocalFragmentReplicationException extends IllegalRegistryActionException {
    private final static EErrorCode _exCode = EErrorCode.LOCAL_FRAGMENT_REPLICATION;

    public LocalFragmentReplicationException(String message) {
        this(message, null);
    }

    public LocalFragmentReplicationException(Throwable cause) {
        this(null, cause);
    }

    public LocalFragmentReplicationException(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
