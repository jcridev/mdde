package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.EWriteCommand;
import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.ArrayList;
import java.util.List;

public class TextSequentialWriteICommandParser<T>  implements ICommandParser<T, EWriteCommand, String>
{
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialWriteICommandParser<T> _writeSequentialCommandParser;

    public TextSequentialWriteICommandParser(IWriteCommandHandler writeCommandHandler, IResponseSerializer<T> serializer) {
        _writeSequentialCommandParser = new SequentialWriteICommandParser<T>(writeCommandHandler, serializer);
    }

    public T runCommand(EWriteCommand EWriteCommand, String arguments) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        if(arguments == null || arguments.isEmpty()){
            return _writeSequentialCommandParser.runCommand(EWriteCommand, new ArrayList<>());
        }
        List<Object> parsedArguments = _stringParser.parseLineArguments(EWriteCommand, arguments);

        return _writeSequentialCommandParser.runCommand(EWriteCommand, parsedArguments);
    }
}
