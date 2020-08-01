package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerBase;
import dev.jcri.mdde.registry.server.responders.WriteCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for WRITE commands that processes commands passed in a form of a string line with argument sequences
 * in accordance to the expected arguments lists of the EStateControlCommand entries.
 * @param <TOut> Command execution result container type.
 */
public class TextSequentialWriteCommandParser<TOut>  implements ICommandParser<TOut, EWriteCommand, String>
{
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialWriteCommandParser<TOut> _writeSequentialCommandParser;
    private final ResponseSerializerBase<TOut> _serializer;

    public TextSequentialWriteCommandParser(WriteCommandResponder writeCommandHandler,
                                            ResponseSerializerBase<TOut> serializer) {
        _writeSequentialCommandParser = new SequentialWriteCommandParser<TOut>(writeCommandHandler, serializer);
        _serializer = serializer;
    }

    public TOut runCommand(EWriteCommand command, String arguments) {
        try {
            if (arguments == null || arguments.isEmpty()) {
                return _writeSequentialCommandParser.runCommand(command, new ArrayList<>());
            }
            List<Object> parsedArguments = _stringParser.parseLineArguments(command, arguments);
            return _writeSequentialCommandParser.runCommand(command, parsedArguments);
        } catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
