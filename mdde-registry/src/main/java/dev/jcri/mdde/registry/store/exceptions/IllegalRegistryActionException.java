package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class IllegalRegistryActionException extends MddeRegistryException {
    public IllegalRegistryActionException(String message){
        super(message);
    }
}
