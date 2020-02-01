package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialWriteCommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.server.responders.WriteCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;

/**
 * Parser for WRITE commands that processes commands passed in a form of a JSON string.
 * @param <TOut> Command execution result container type
 */
public class JsonWriteCommandParser<TOut> extends JsonCommandParserBase
        implements ICommandParser<TOut, EWriteCommand, String> {
    private final SequentialWriteCommandParser<TOut> _sequentialCommandParser;
    private final IResponseSerializer<TOut> _serializer;

    public JsonWriteCommandParser(WriteCommandResponder writeCommandHandler, IResponseSerializer<TOut> serializer){
        _sequentialCommandParser = new SequentialWriteCommandParser<>(writeCommandHandler, serializer);
        _serializer = serializer;
    }

    @Override
    public TOut runCommand(EWriteCommand command, String arguments) {
        try {
            var parsedArguments = parseArguments(command, arguments);
            return _sequentialCommandParser.runCommand(command, parsedArguments);
        } catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
