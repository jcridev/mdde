package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.EReadCommand;
import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialReadICommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

public class JsonReadCommandParser<T> extends JsonCommandParserBase implements ICommandParser<T, EReadCommand, String> {

    private final SequentialReadICommandParser<T> _sequentialCommandParser;
    private final IResponseSerializer<T> _serializer;

    public JsonReadCommandParser(IReadCommandHandler readCommandHandler, IResponseSerializer<T> serializer){
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
