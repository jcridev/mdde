package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class RegistryModeAlreadySetException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.REGISTRY_MODE_ALREADY_SET;

    public RegistryModeAlreadySetException(String message, Throwable cause){
        super(_exCode, message, cause);
    }

    public RegistryModeAlreadySetException(String message){
        this(message, null);
    }

    public RegistryModeAlreadySetException(Throwable cause){
        this(null, cause);
    }
}
