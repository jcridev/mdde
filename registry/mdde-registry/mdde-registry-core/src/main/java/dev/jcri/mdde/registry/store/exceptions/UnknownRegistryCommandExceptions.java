package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Unknown command supplied to the interpreter
 */
public class UnknownRegistryCommandExceptions extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.UNKNOWN_COMMAND;

    public UnknownRegistryCommandExceptions(String command){
        super(_exCode, String.format("There is no command defined '%s'", command == null ? "_null_" : command));
    }
}
