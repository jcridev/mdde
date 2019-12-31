package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

/**
 *
 * @param <Tout> Command execution result
 * @param <Tc> Command type (Read / Write)
 * @param <Targs> Arguments in the format understandable to the specific parser
 */
public interface ICommandParser<Tout, Tc extends ICommand, Targs> {
    Tout runCommand(Tc command, Targs arguments)
            throws UnknownRegistryCommandExceptions, MddeRegistryException;
}
