package dev.jcri.mdde.registry.store.exceptions.snapshot;

import dev.jcri.mdde.registry.exceptions.EErrorCode;

/**
 * Exception thrown when the registry is unable to clear out the snapshot
 */
public class FailedToDeleteSnapshot extends RegistrySnapshotException {
    private final static EErrorCode _exCode = EErrorCode.FAILED_TO_DELETE_SNAPSHOT;

    public FailedToDeleteSnapshot(String message) {
        this(message, null);
    }

    public FailedToDeleteSnapshot(Throwable cause) {
        this(null, cause);
    }

    public FailedToDeleteSnapshot(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
