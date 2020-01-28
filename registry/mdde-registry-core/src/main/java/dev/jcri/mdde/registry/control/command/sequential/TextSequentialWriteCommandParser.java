package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.server.responders.WriteCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;

import java.util.ArrayList;
import java.util.List;

public class TextSequentialWriteCommandParser<T>  implements ICommandParser<T, EWriteCommand, String>
{
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialWriteCommandParser<T> _writeSequentialCommandParser;
    private final IResponseSerializer<T> _serializer;

    public TextSequentialWriteCommandParser(WriteCommandResponder writeCommandHandler, IResponseSerializer<T> serializer) {
        _writeSequentialCommandParser = new SequentialWriteCommandParser<T>(writeCommandHandler, serializer);
        _serializer = serializer;
    }

    public T runCommand(EWriteCommand EWriteCommand, String arguments) {
        try {
            if (arguments == null || arguments.isEmpty()) {
                return _writeSequentialCommandParser.runCommand(EWriteCommand, new ArrayList<>());
            }
            List<Object> parsedArguments = _stringParser.parseLineArguments(EWriteCommand, arguments);
            return _writeSequentialCommandParser.runCommand(EWriteCommand, parsedArguments);
        } catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
