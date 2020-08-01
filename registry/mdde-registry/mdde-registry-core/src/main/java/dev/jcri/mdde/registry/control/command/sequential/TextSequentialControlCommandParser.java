package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerBase;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.store.RegistryStateCommandHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for CONTROL commands that processes commands passed in a form of a string line with argument sequences
 * in accordance to the expected arguments lists of the EStateControlCommand entries.
 * @param <TOut> Command execution result container type.
 */
public class TextSequentialControlCommandParser<TOut>  implements ICommandParser<TOut, EStateControlCommand, String>
{
    private final ISequenceParser _stringParser = new SimpleSequenceParser();
    private final SequentialControlCommandParser<TOut> _writeSequentialCommandParser;
    private final ResponseSerializerBase<TOut> _serializer;

    public TextSequentialControlCommandParser(RegistryStateCommandHandler commandHandler,
                                              ResponseSerializerBase<TOut> serializer) {
        _writeSequentialCommandParser = new SequentialControlCommandParser<TOut>(commandHandler, serializer);
        _serializer = serializer;
    }

    public TOut runCommand(EStateControlCommand command, String arguments) {
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
