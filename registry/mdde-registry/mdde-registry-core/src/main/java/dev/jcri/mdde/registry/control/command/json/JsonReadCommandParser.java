package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialReadCommandParser;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerBase;
import dev.jcri.mdde.registry.server.responders.ReadCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;

/**
 * Parser for READ commands that processes commands passed in a form of a JSON string.
 * @param <TOut> Command execution result container type
 */
public class JsonReadCommandParser<TOut> extends JsonCommandParserBase
        implements ICommandParser<TOut, EReadCommand, String> {
    private final SequentialReadCommandParser<TOut> _sequentialCommandParser;
    private final ResponseSerializerBase<TOut> _serializer;

    public JsonReadCommandParser(ReadCommandResponder readCommandHandler, ResponseSerializerBase<TOut> serializer){
        _sequentialCommandParser = new SequentialReadCommandParser<>(readCommandHandler, serializer);
        _serializer = serializer;
    }

    @Override
    public TOut runCommand(EReadCommand command, String arguments) {
        try {
            var parsedArguments = parseArguments(command, arguments);
            return _sequentialCommandParser.runCommand(command, parsedArguments);
        }catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
