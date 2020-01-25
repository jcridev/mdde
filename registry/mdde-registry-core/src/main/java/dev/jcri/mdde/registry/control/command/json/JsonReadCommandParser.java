package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialReadICommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.server.responders.ReadCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;

public class JsonReadCommandParser<T> extends JsonCommandParserBase implements ICommandParser<T, EReadCommand, String> {

    private final SequentialReadICommandParser<T> _sequentialCommandParser;
    private final IResponseSerializer<T> _serializer;

    public JsonReadCommandParser(ReadCommandResponder readCommandHandler, IResponseSerializer<T> serializer){
        _sequentialCommandParser = new SequentialReadICommandParser<>(readCommandHandler, serializer);
        _serializer = serializer;
    }

    @Override
    public T runCommand(EReadCommand command, String arguments) {
        try {
            var parsedArguments = parseArguments(command, arguments);
            return _sequentialCommandParser.runCommand(command, parsedArguments);
        }catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
