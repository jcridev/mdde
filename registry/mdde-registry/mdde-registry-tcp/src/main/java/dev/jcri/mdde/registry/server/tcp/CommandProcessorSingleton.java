package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.server.CommandProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * A singleton wrapper around a CommandProcessor to simplify access within the TCP server operations.
 * We don't want more than one CommandProcessor instance to exists, that communicate to the same DB nodes
 * and the same backend registry.
 */
public class CommandProcessorSingleton {
    private static final Logger logger = LogManager.getLogger(CommandProcessorSingleton.class);
    private static class LazyHolder {
        private static CommandProcessorSingleton _instance = new CommandProcessorSingleton();
    }
    private CommandProcessorSingleton(){}

    public static CommandProcessorSingleton getDefaultInstance(){
        return LazyHolder._instance;
    }

    private CommandProcessor<String, String, String> _commandProcessor = null;

    /**
     * Assign a command processor that accepts strings passed from the TCP server and returns strings as well
     * that will be passed back through the TCP.
     *
     * Initialization is only possible once after the application was started.
     * Any concurrency, if and when allowed, must be handled by the CommandProcessor itself.
     * @param processor CommandProcessor<String, String, String>
     */
    public synchronized void initializeCommandProcessor(CommandProcessor<String, String, String> processor){
        Objects.requireNonNull(processor, "CommandProcessor instance can't be set to null");
        if(_commandProcessor != null){
            throw new IllegalStateException("CommandProcessorSingleton was already initialized and can't be re-initialized");
        }
        _commandProcessor = processor;
    }



    /**
     * Get the instance of the CommandProcessor that was passed to the initializer method
     * @return CommandProcessor instance or the null if CommandProcessorSingleton was not initialized
     */
    public CommandProcessor<String, String, String> getCommandProcessor(){
        return _commandProcessor;
    }
}
