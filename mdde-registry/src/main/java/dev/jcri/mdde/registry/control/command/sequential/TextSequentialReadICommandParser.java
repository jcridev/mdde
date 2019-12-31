package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.EReadCommand;
import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.ArrayList;
import java.util.List;

public class TextSequentialReadICommandParser<T> implements ICommandParser<T, EReadCommand, String> {
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialReadICommandParser<T> _sequentialCommandParser;

    public TextSequentialReadICommandParser(IReadCommandHandler readCommandHandler, IResponseSerializer<T> serializer) {
        _sequentialCommandParser = new SequentialReadICommandParser<>(readCommandHandler, serializer);
    }

    public T runCommand(EReadCommand EReadCommand, String arguments) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        if(arguments == null || arguments.isEmpty()){
            return _sequentialCommandParser.runCommand(EReadCommand, new ArrayList<>());
        }
        List<Object> parsedArguments = _stringParser.parseLineArguments(EReadCommand, arguments);

        return _sequentialCommandParser.runCommand(EReadCommand, parsedArguments);
    }
}
