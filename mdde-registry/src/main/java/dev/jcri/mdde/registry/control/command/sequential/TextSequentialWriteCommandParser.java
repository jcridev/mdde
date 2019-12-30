package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.WriteCommands;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.ArrayList;
import java.util.List;

public class TextSequentialWriteCommandParser<T> extends SequentialWriteCommandParser<T> {
    private final ISequenceParser _stringParser = new SimpleSequenceParser();

    public TextSequentialWriteCommandParser(IWriteCommandHandler writeCommandHandler, IResponseSerializer<T> serializer) {
        super(writeCommandHandler, serializer);
    }

    public T runCommand(WriteCommands writeCommand, String arguments) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        if(arguments == null || arguments.isEmpty()){
            return super.runCommand(writeCommand, new ArrayList<>());
        }
        List<Object> parsedArguments = _stringParser.parseLineArguments(writeCommand, arguments);

        return super.runCommand(writeCommand, parsedArguments);
    }

    public T runCommand(String command) throws UnknownRegistryCommandExceptions, MddeRegistryException {
        var parseCommand =  _stringParser.getIsWriteCommandKeyword(command);

        return this.runCommand(parseCommand, command);
    }
}
