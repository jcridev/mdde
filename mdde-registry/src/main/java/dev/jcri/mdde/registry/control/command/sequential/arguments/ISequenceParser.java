package dev.jcri.mdde.registry.control.command.sequential.arguments;

import dev.jcri.mdde.registry.control.EWriteCommand;
import dev.jcri.mdde.registry.control.ICommand;
import dev.jcri.mdde.registry.control.EReadCommand;
import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.List;

public interface ISequenceParser {
    /**
     * Get only the part of the command that contains arguments
     * @param command Full command to split
     * @return Part of the command containing arguments
     */
    CommandComponents<String> getArgumentsFromLine(String command);

    /**
     * Convert the string command into the expected list of objects
     * @param command Exact type of the command
     * @param arguments Use getArgumentsFromLine to get this argument
     * @return
     * @throws UnknownRegistryCommandExceptions
     */
    List<Object> parseLineArguments(ICommand command, String arguments) throws UnknownRegistryCommandExceptions;
}
