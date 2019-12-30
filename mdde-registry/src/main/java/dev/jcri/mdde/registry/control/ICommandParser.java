package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

/**
 *
 * @param <T> Command execution result
 * @param <Tc> Command type (Read / Write)
 * @param <Ta> Arguments in the format understandable to the specific parser
 */
public interface ICommandParser<T, Tc extends ICommand, Ta> {
    T runCommand(Tc command, Ta arguments)
            throws UnknownRegistryCommandExceptions, MddeRegistryException;
}
