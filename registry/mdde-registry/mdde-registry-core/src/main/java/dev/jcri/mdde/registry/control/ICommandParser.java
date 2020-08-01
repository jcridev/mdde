package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.shared.commands.ICommand;

/**
 *
 * @param <TOut> Command execution result.
 * @param <TCmd> Command type (Read,Write,State control,...).
 * @param <TArgs> Arguments in the format understandable to the specific parser.
 */
public interface ICommandParser<TOut, TCmd extends ICommand, TArgs> {
    TOut runCommand(TCmd command, TArgs arguments);
}
