package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.server.CommandProcessor;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.shared.commands.containers.CommandInputContainer;
import dev.jcri.mdde.registry.shared.commands.containers.CommandSerializationHelper;
import dev.jcri.mdde.registry.shared.commands.containers.args.WriteArgsPopulateNodesContainer;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
     * Pre-populate default nodes in the registry
     * @param networkDBNodes
     */
    public synchronized void initializeDefaultNodes(List<DBNetworkNodesConfiguration> networkDBNodes)
        throws IOException {
        if(_commandProcessor == null){
            throw new IllegalStateException("CommandProcessorSingleton is not initialized");
        }
        var defaultNodesParam = new HashSet<String>();
        for(var node: networkDBNodes){
            if(!node.getDefaultNode()){
                continue;
            }
            if(!defaultNodesParam.add(node.getNodeId())){
                throw new IllegalArgumentException(String.format("Duplicate node id: %s", node.getNodeId()));
            }
        }

        if(defaultNodesParam.size() == 0){
            return;
        }

        WriteArgsPopulateNodesContainer newNodesArg = new WriteArgsPopulateNodesContainer();
        newNodesArg.setNodes(defaultNodesParam);
        String populateCommand = CommandSerializationHelper.serializeJson(EWriteCommand.POPULATE_NODES, newNodesArg);
        var populateNodesResponse = _commandProcessor.processIncomingStatement(populateCommand);
        logger.info(populateNodesResponse);
    }

    /**
     * Get the instance of the CommandProcessor that was passed to the initializer method
     * @return CommandProcessor instance or the null if CommandProcessorSingleton was not initialized
     */
    public CommandProcessor<String, String, String> getCommandProcessor(){
        return _commandProcessor;
    }
}
