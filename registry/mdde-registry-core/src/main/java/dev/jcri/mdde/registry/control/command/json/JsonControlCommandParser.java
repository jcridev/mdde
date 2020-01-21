package dev.jcri.mdde.registry.control.command.json;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialControlCommandParser;
import dev.jcri.mdde.registry.control.command.sequential.SequentialReadICommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.RegistryStateCommandHandler;

public class JsonControlCommandParser<T> extends JsonCommandParserBase
        implements ICommandParser<T, EStateControlCommand, String> {

    private final SequentialControlCommandParser<T> _sequentialCommandParser;
    private final IResponseSerializer<T> _serializer;

    /**
     * Constructor
     * @param commandHandler Current instance of the RegistryStateCommandHandler
     * @param serializer Serializer instance
     */
    public JsonControlCommandParser(RegistryStateCommandHandler commandHandler, IResponseSerializer<T> serializer){
        _sequentialCommandParser = new SequentialControlCommandParser<>(commandHandler, serializer);
        _serializer = serializer;
    }

    @Override
    public T runCommand(EStateControlCommand command, String arguments) {
        try {
            var parsedArguments = parseArguments(command, arguments);
            return _sequentialCommandParser.runCommand(command, parsedArguments);
        }catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }
}
