package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Unknown command supplied to the interpreter
 */
public class UnknownRegistryCommandExceptions extends MddeRegistryException {
    public UnknownRegistryCommandExceptions(String command){
        super(String.format("There is no command defined '%s'", command == null ? "_null_" : command));
    }
}
