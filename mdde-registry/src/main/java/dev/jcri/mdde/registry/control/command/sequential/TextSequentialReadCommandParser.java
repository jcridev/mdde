package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ReadCommands;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.ArrayList;
import java.util.List;

public class TextSequentialReadCommandParser<T> extends SequentialReadCommandParser<T> {
    private final ISequenceParser _stringParser = new SimpleSequenceParser();

    public TextSequentialReadCommandParser(IReadCommandHandler readCommandHandler, IResponseSerializer<T> serializer) {
        super(readCommandHandler, serializer);
    }

    public T runCommand(ReadCommands readCommand, String arguments) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        if(arguments == null || arguments.isEmpty()){
            return super.runCommand(readCommand, new ArrayList<>());
        }
        List<Object> parsedArguments = _stringParser.parseLineArguments(readCommand, arguments);

        return super.runCommand(readCommand, parsedArguments);
    }

    public T runCommand(String command) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        var parseCommand =  _stringParser.getIsReadCommandKeyword(command);

        return this.runCommand(parseCommand, command);
    }
}
