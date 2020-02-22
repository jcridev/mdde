package dev.jcri.mdde.registry.server;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.ICommandPreProcessor;
import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.control.serialization.IResponseExceptionSerializer;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Entry point for the incoming Registry reading or altering commands. These are the interfaces used by the learners.
 * @param <TIn> Type of incoming statements
 * @param <TArgs> Type of the arguments object
 * @param <TOut> Type of the returned processing values
 */
public final class CommandProcessor<TIn, TArgs, TOut> {
    private static final Logger logger = LogManager.getLogger(CommandProcessor.class);

    private final ICommandPreProcessor<TArgs, TIn> _commandPreProcessor;
    private final ICommandParser<TOut, EStateControlCommand, TArgs> _controlCommandParser;
    private final ICommandParser<TOut, EReadCommand, TArgs> _readCommandParser;
    private final ICommandParser<TOut, EWriteCommand, TArgs> _writeCommandParser;
    private final IResponseExceptionSerializer<TOut> _errorSerializer;

    /**
     * Constructor
     * @param commandPreProcessor Pre-processor of the incoming statement. Splits the statement into the keyword and
     *                            arguments object. The split statement is them processed by the appropriate parser.
     * @param controlCommandParser Initialized implementation of the registry state control statement parser.
     * @param readCommandParser Initialized implementation of the registry read statement parser.
     * @param writeCommandParser Initialized implementation of the registry write statement parser.
     * @param errorSerializer Appropriate response serialize used to return a parsing or statement processing error in
     *                        the appropriate format.
     */
    public CommandProcessor(ICommandPreProcessor<TArgs, TIn> commandPreProcessor,
                            ICommandParser<TOut, EStateControlCommand, TArgs> controlCommandParser,
                            ICommandParser<TOut, EReadCommand, TArgs> readCommandParser,
                            ICommandParser<TOut, EWriteCommand, TArgs> writeCommandParser,
                            IResponseExceptionSerializer<TOut> errorSerializer)
    {
        Objects.requireNonNull(commandPreProcessor, "commandPreProcessor can't be null");
        Objects.requireNonNull(controlCommandParser, "controlCommandParser can't be null");
        Objects.requireNonNull(readCommandParser, "readCommandParser can't be null");
        Objects.requireNonNull(writeCommandParser, "writeCommandParser can't be null");
        Objects.requireNonNull(errorSerializer, "errorSerializer can't be null");

        _commandPreProcessor = commandPreProcessor;
        _controlCommandParser = controlCommandParser;
        _readCommandParser = readCommandParser;
        _writeCommandParser = writeCommandParser;
        _errorSerializer = errorSerializer;
    }

    public TOut processIncomingStatement(TIn statement){
        try {
            Objects.requireNonNull(statement, "statement can't be null");
            logger.trace("Incoming statement: '{}'", statement.toString());

            // Split the statement (get keyword separately from the arguments)
            CommandComponents<TArgs> components = null;
            components = _commandPreProcessor.splitIncoming(statement);

            // Determine type of the command
            EReadCommand readCommand = null;
            EWriteCommand writeCommand = null;
            EStateControlCommand stateControlCommand = null;

            TOut result = null;
            if((stateControlCommand = components.tryGetIsStateControlCommandKeyword()) != null){
                logger.trace("Incoming statement is CONTROL");
                // Is state control command
                result = _controlCommandParser.runCommand(stateControlCommand, components.getArgs());
            }
            else if ((readCommand = components.tryGetIsReadCommandKeyword()) != null) {
                logger.trace("Incoming statement is READ");
                // It's read command
                result = _readCommandParser.runCommand(readCommand, components.getArgs());
            } else if (((writeCommand = components.tryGetIsWriteCommandKeyword())) != null) {
                logger.trace("Incoming statement is WRITE");
                // It's write command
                result = _writeCommandParser.runCommand(writeCommand, components.getArgs());
            } else {
                logger.trace("Incoming statement is UNKNOWN");
                // It's unknown command
                throw new UnknownRegistryCommandExceptions(String.format("Command %s was not found in the registry",
                        components.getKeyword()));
            }

            return result;
        } catch (Exception e) {
            return _errorSerializer.serializeException(e);
        }
    }
}
