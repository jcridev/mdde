package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerBase;
import dev.jcri.mdde.registry.server.responders.ReadCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for READ commands that processes commands passed in a form of a string line with argument sequences
 * in accordance to the expected arguments lists of the EStateControlCommand entries.
 * @param <TOut> Command execution result container type.
 */
public class TextSequentialReadCommandParser<TOut> implements ICommandParser<TOut, EReadCommand, String> {
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialReadCommandParser<TOut> _sequentialCommandParser;
    private final ResponseSerializerBase<TOut> _serializer;

    public TextSequentialReadCommandParser(ReadCommandResponder readCommandHandler,
                                           ResponseSerializerBase<TOut> serializer) {
        _sequentialCommandParser = new SequentialReadCommandParser<>(readCommandHandler, serializer);
        _serializer = serializer;
    }

    public TOut runCommand(EReadCommand command, String arguments) {
        try{
            if(arguments == null || arguments.isEmpty()){
                return _sequentialCommandParser.runCommand(command, new ArrayList<>());
            }
            List<Object> parsedArguments = _stringParser.parseLineArguments(command, arguments);

            return _sequentialCommandParser.runCommand(command, parsedArguments);
        } catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
