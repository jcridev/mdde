package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.control.exceptions.MalformedCommandStatementException;

/**
 * Implement this interface with a class that splits the command to the keyword and its arguments.
 * @param <TArgs> Type of the expected command arguments after split.
 * @param <TIn> Type of the incoming data.
 */
public interface ICommandPreProcessor<TArgs, TIn> {
    CommandComponents<TArgs> splitIncoming(TIn statement) throws MalformedCommandStatementException;
}
