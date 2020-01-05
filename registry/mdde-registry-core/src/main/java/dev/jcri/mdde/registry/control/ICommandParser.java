package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.shared.commands.ICommand;

/**
 *
 * @param <Tout> Command execution result
 * @param <Tc> Command type (Read / Write)
 * @param <Targs> Arguments in the format understandable to the specific parser
 */
public interface ICommandParser<Tout, Tc extends ICommand, Targs> {
    Tout runCommand(Tc command, Targs arguments);
}
