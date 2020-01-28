package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialWriteCommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.server.responders.WriteCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;

public class JsonWriteCommandParser<T> extends JsonCommandParserBase implements ICommandParser<T, EWriteCommand, String> {
    private final SequentialWriteCommandParser<T> _sequentialCommandParser;
    private final IResponseSerializer<T> _serializer;

    public JsonWriteCommandParser(WriteCommandResponder writeCommandHandler, IResponseSerializer<T> serializer){
        _sequentialCommandParser = new SequentialWriteCommandParser<>(writeCommandHandler, serializer);
        _serializer = serializer;
    }

    @Override
    public T runCommand(EWriteCommand command, String arguments) {
        try {
            var parsedArguments = parseArguments(command, arguments);
            return _sequentialCommandParser.runCommand(command, parsedArguments);
        } catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
