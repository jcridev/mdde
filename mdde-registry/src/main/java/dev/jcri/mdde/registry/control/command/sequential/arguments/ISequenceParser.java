package dev.jcri.mdde.registry.control.command.sequential.arguments;

import dev.jcri.mdde.registry.control.ICommands;
import dev.jcri.mdde.registry.control.ReadCommands;
import dev.jcri.mdde.registry.control.WriteCommands;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.List;

public interface ISequenceParser {

    ReadCommands getIsReadCommandKeyword(String command) throws UnknownRegistryCommandExceptions;
    WriteCommands getIsWriteCommandKeyword(String command) throws UnknownRegistryCommandExceptions;
    ReadCommands tryGetIsReadCommandKeyword(String command);
    WriteCommands tryGetIsWriteCommandKeyword(String command);
    List<Object> parseLineArguments(ICommands readCommand, String arguments) throws UnknownRegistryCommandExceptions;
}
