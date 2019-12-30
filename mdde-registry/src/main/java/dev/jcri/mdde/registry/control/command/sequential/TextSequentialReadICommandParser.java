package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.ReadCommand;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.ArrayList;
import java.util.List;

public class TextSequentialReadICommandParser<T> implements ICommandParser<T, ReadCommand, String> {
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialReadICommandParser<T> _sequentialCommandParser;

    public TextSequentialReadICommandParser(IReadCommandHandler readCommandHandler, IResponseSerializer<T> serializer) {
        _sequentialCommandParser = new SequentialReadICommandParser<>(readCommandHandler, serializer);
    }

    public T runCommand(ReadCommand readCommand, String arguments) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        if(arguments == null || arguments.isEmpty()){
            return _sequentialCommandParser.runCommand(readCommand, new ArrayList<>());
        }
        List<Object> parsedArguments = _stringParser.parseLineArguments(readCommand, arguments);

        return _sequentialCommandParser.runCommand(readCommand, parsedArguments);
    }
}
