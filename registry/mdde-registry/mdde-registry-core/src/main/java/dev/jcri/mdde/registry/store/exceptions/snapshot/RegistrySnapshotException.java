package dev.jcri.mdde.registry.store.exceptions.snapshot;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public abstract  class RegistrySnapshotException extends MddeRegistryException {
    public RegistrySnapshotException(EErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public RegistrySnapshotException(EErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public RegistrySnapshotException(EErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
