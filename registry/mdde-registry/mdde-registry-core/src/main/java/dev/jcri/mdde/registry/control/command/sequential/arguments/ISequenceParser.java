package dev.jcri.mdde.registry.control.command.sequential.arguments;

import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.shared.commands.ICommand;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.List;

public interface ISequenceParser {
    /**
     * Get only the part of the command that contains arguments.
     * @param command Full command to split.
     * @return Part of the command containing arguments.
     */
    CommandComponents<String> getArgumentsFromLine(String command);

    /**
     * Convert the string command into the expected list of objects.
     * @param command Exact type of the command.
     * @param arguments Use getArgumentsFromLine to get this argument.
     * @return List of command statement arguments as `Object` types. Additional casting required later down the
     * processing pipeline.
     * @throws UnknownRegistryCommandExceptions Command statement is unknown or malformed.
     */
    List<Object> parseLineArguments(ICommand command, String arguments) throws UnknownRegistryCommandExceptions;
}
