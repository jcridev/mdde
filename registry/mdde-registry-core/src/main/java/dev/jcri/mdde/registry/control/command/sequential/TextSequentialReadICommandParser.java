package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.server.responders.ReadCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;

import java.util.ArrayList;
import java.util.List;

public class TextSequentialReadICommandParser<T> implements ICommandParser<T, EReadCommand, String> {
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialReadICommandParser<T> _sequentialCommandParser;
    private final IResponseSerializer<T> _serializer;

    public TextSequentialReadICommandParser(ReadCommandResponder readCommandHandler, IResponseSerializer<T> serializer) {
        _sequentialCommandParser = new SequentialReadICommandParser<>(readCommandHandler, serializer);
        _serializer = serializer;
    }

    public T runCommand(EReadCommand EReadCommand, String arguments) {
        try{
            if(arguments == null || arguments.isEmpty()){
                return _sequentialCommandParser.runCommand(EReadCommand, new ArrayList<>());
            }
            List<Object> parsedArguments = _stringParser.parseLineArguments(EReadCommand, arguments);

            return _sequentialCommandParser.runCommand(EReadCommand, parsedArguments);
        } catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
