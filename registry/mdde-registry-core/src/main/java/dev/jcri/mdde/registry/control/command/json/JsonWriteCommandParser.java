package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.EWriteCommand;
import dev.jcri.mdde.registry.control.ICommandParser;

import dev.jcri.mdde.registry.control.command.sequential.SequentialWriteICommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;

public class JsonWriteCommandParser<T> extends JsonCommandParserBase implements ICommandParser<T, EWriteCommand, String> {
    private final SequentialWriteICommandParser<T> _sequentialCommandParser;
    private final IResponseSerializer<T> _serializer;

    public JsonWriteCommandParser(IWriteCommandHandler writeCommandHandler, IResponseSerializer<T> serializer){
        _sequentialCommandParser = new SequentialWriteICommandParser<>(writeCommandHandler, serializer);
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
