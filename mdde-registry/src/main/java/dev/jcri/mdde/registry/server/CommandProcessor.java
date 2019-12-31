package dev.jcri.mdde.registry.server;

import dev.jcri.mdde.registry.control.*;
import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.control.exceptions.MalformedCommandStatementException;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.server.nettytcp.Listener;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 *
 * @param <Tin> Type of incoming statements
 * @param <Targs> Type of the arguments object
 * @param <Tout> Type of the returned processing values
 */
public class CommandProcessor<Tin, Targs, Tout> {
    private static final Logger logger = LogManager.getLogger(Listener.class);

    private final ICommandPreProcessor<Targs, Tin> _commandPreProcessor;
    private final ICommandParser<Tout, EReadCommand, Targs> _readCommandParser;
    private final ICommandParser<Tout, EWriteCommand, Targs> _writeCommandParser;

    public CommandProcessor(ICommandPreProcessor<Targs, Tin> commandPreProcessor,
                            ICommandParser<Tout, EReadCommand, Targs> readCommandParser,
                            ICommandParser<Tout, EWriteCommand, Targs> writeCommandParser)
    {
        Objects.requireNonNull(commandPreProcessor, "commandPreProcessor can't be null");
        Objects.requireNonNull(readCommandParser, "readCommandParser can't be null");
        Objects.requireNonNull(writeCommandParser, "writeCommandParser can't be null");

        _commandPreProcessor = commandPreProcessor;
        _readCommandParser = readCommandParser;
        _writeCommandParser = writeCommandParser;
    }

    public Tout processIncomingStatement(Tin statement){
        Objects.requireNonNull(statement, "statement can't be null");

        // Split the statement (get keyword separately from the arguments)
        CommandComponents<Targs> components = null;
        try {
            components = _commandPreProcessor.splitIncoming(statement);
        } catch (MalformedCommandStatementException e) {
            e.printStackTrace();
        }

        // Determine type of the command
        EReadCommand readCommand = null;
        EWriteCommand writeCommand = null;
        Tout result = null;
        try {
            if((readCommand = components.tryGetIsReadCommandKeyword()) != null){
                // It's read command
                result =  _readCommandParser.runCommand(readCommand, components.getArgs());
            }
            else if (((writeCommand = components.tryGetIsWriteCommandKeyword())) != null){
                // It's write command
                result =  _writeCommandParser.runCommand(writeCommand, components.getArgs());
            }
            else{
                // It's unknown command
                throw new UnknownRegistryCommandExceptions(String.format("Command %s was not found in the registry",
                            components.getKeyword()));
            }
        } catch (UnknownRegistryCommandExceptions unknownRegistryCommandExceptions) {
            unknownRegistryCommandExceptions.printStackTrace();
        }
        catch (MddeRegistryException e){
            e.printStackTrace();
        }

        return result;
    }
}
