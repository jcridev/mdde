package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.EWriteCommand;
import dev.jcri.mdde.registry.control.ICommandParser;

import dev.jcri.mdde.registry.control.command.sequential.SequentialWriteICommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

public class JsonWriteICommandParser<T> extends JsonCommandParserBase implements ICommandParser<T, EWriteCommand, String> {
    private final SequentialWriteICommandParser<T> _sequentialCommandParser;

    public JsonWriteICommandParser(IWriteCommandHandler writeCommandHandler, IResponseSerializer<T> serializer){
        _sequentialCommandParser = new SequentialWriteICommandParser<>(writeCommandHandler, serializer);
    }

    @Override
    public T runCommand(EWriteCommand command, String arguments) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        var parsedArguments = parseArguments(command, arguments);
        return _sequentialCommandParser.runCommand(command, parsedArguments);
    }
}
