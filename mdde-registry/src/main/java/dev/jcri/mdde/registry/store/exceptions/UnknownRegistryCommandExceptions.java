package dev.jcri.mdde.registry.store.exceptions;

/**
 * Unknown command supplied to the interpreter
 */
public class UnknownRegistryCommandExceptions extends Exception {
    public UnknownRegistryCommandExceptions(String command){
        super(String.format("There is no command defined '%s'", command == null ? "_null_" : command));
    }
}
