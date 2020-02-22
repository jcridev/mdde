package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.RegistryStateCommandHandler;

import java.util.Optional;

public class IllegalRegistryModeException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.INCORRECT_REGISTRY_MODE_FOR_OPERATION;

    public IllegalRegistryModeException(RegistryStateCommandHandler.ERegistryState currentState,
                                        RegistryStateCommandHandler.ERegistryState requiredState,
                                        Throwable cause){
        super(_exCode,
                String.format("Registry is incorrect mode '%s' for the operation, must be in '%s'",
                        currentState != null ? currentState.name() : "null",
                        requiredState != null ? requiredState.name() : "null"),
                cause);
    }

    public IllegalRegistryModeException(RegistryStateCommandHandler.ERegistryState currentState,
                                        RegistryStateCommandHandler.ERegistryState requiredState){
        this(currentState, requiredState, null);
    }
}
