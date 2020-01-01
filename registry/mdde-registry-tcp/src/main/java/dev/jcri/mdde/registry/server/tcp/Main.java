package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;
import dev.jcri.mdde.registry.control.EReadCommand;
import dev.jcri.mdde.registry.control.EWriteCommand;
import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.ICommandPreProcessor;
import dev.jcri.mdde.registry.control.command.json.JsonCommandPreProcessor;
import dev.jcri.mdde.registry.control.command.json.JsonReadCommandParser;
import dev.jcri.mdde.registry.control.command.json.JsonWriteCommandParser;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerJson;
import dev.jcri.mdde.registry.server.CommandProcessor;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;
import dev.jcri.mdde.registry.store.impl.redis.ReadCommandHandlerRedis;
import dev.jcri.mdde.registry.store.impl.redis.RedisConnectionHelper;
import dev.jcri.mdde.registry.store.impl.redis.WriteCommandHandlerRedis;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static Listener _listener;

    public static void main(String args[]){
        AppParams parsedArgs = null;
        try {
            parsedArgs = parseArgs(args);
        }catch (Exception ex){
            logger.error(ex);
            System.err.println(ex.getMessage());
            return;
        }
        // Configure redis registry store
        var testConfig = new RegistryStoreConfigRedis(){
            {setHost("localhost");}  // TODO: Read actual config
            {setPort(6379);}
            {setPassword(null);}
        };

        var redisConnection = new RedisConnectionHelper(testConfig);

        // Handle commands
        IReadCommandHandler readCommandHandler = new ReadCommandHandlerRedis(redisConnection);
        IWriteCommandHandler writeCommandHandler = new WriteCommandHandlerRedis(redisConnection, readCommandHandler);
        // Parse commands
        IResponseSerializer<String> responseSerializer = new ResponseSerializerJson();
        ICommandParser<String, EReadCommand, String> readCommandParser = new JsonReadCommandParser<>(readCommandHandler, responseSerializer);
        ICommandParser<String, EWriteCommand, String> writeCommandParser = new JsonWriteCommandParser<>(writeCommandHandler, responseSerializer);
        ICommandPreProcessor<String, String> commandPreProcessor = new JsonCommandPreProcessor();
        // Incoming statements processor
        var commandProcessor = new CommandProcessor<String, String, String>(commandPreProcessor, readCommandParser, writeCommandParser, responseSerializer);
        CommandProcessorSingleton.getDefaultInstance().initializeCommandProcessor(commandProcessor);
        // TCP calls listener
        _listener = new Listener();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if(_listener != null){
                    _listener.stop();
                    logger.info("Stopped the listener.");
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }));
        try {
            _listener.start(parsedArgs.tcpPort);
        }
        catch (Exception ex){
            logger.error(ex);
            System.err.println(ex.getMessage());
        }
    }

    private static AppParams parseArgs(String[] args){
        final String portTag = "-p";
        final String configPathTag = "-c";

        if(args.length < 4){
            throw new IllegalArgumentException(
                    MessageFormat.format("Port number parameter {} and path to config {} are required.",
                            portTag, configPathTag)
            );
        }
        int port = -1;
        Path configFilePath = null;
        Map<String, String> argsMap = new HashMap<>();
        for(int i = 0; i < args.length; i = i+2){
            var tag = args[i];
            if(i+1 >= args.length){
                throw new IllegalArgumentException(MessageFormat.format("Parameter {} passed without a value", tag));
            }
            var val = args[i+1];
            argsMap.put(tag, val);
        }

        var portStr = getArgParam(argsMap, portTag);
        port = Integer.parseInt(portStr);

        var configPathString =getArgParam(argsMap, configPathTag);
        configFilePath = Path.of(configPathString);

        return new AppParams(configFilePath, port);
    }

    private static String getArgParam(Map<String, String> argsMap, String tag){
        var value = argsMap.get(tag);
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException(MessageFormat.format("Parameter {} passed without a value", tag));
        }
        return value;
    }


    private static final class AppParams{
        private final Path pathToConfigFile;
        private final int tcpPort;

        private AppParams(Path pathToConfigFile, int tcpPort) {
            Objects.requireNonNull(pathToConfigFile, "Path to MDDE Registry config can't be null");
            this.pathToConfigFile = pathToConfigFile;
            this.tcpPort = tcpPort;
        }

        public Path getPathToConfigFile() {
            return pathToConfigFile;
        }

        public int getTcpPort() {
            return tcpPort;
        }
    }
}
