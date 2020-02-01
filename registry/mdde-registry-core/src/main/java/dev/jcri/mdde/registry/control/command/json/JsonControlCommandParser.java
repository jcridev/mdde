package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialControlCommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.store.RegistryStateCommandHandler;

/**
 * Parser for CONTROL commands that processes commands passed in a form of a JSON string.
 * @param <TOut> Command execution result container type
 */
public class JsonControlCommandParser<TOut> extends JsonCommandParserBase
        implements ICommandParser<TOut, EStateControlCommand, String> {
    private final SequentialControlCommandParser<TOut> _sequentialCommandParser;
    private final IResponseSerializer<TOut> _serializer;

    /**
     * Constructor
     * @param commandHandler Current instance of the RegistryStateCommandHandler
     * @param serializer Serializer instance
     */
    public JsonControlCommandParser(RegistryStateCommandHandler commandHandler, IResponseSerializer<TOut> serializer){
        _sequentialCommandParser = new SequentialControlCommandParser<>(commandHandler, serializer);
        _serializer = serializer;
    }

    @Override
    public TOut runCommand(EStateControlCommand command, String arguments) {
        try {
            var parsedArguments = parseArguments(command, arguments);
            return _sequentialCommandParser.runCommand(command, parsedArguments);
        }catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
