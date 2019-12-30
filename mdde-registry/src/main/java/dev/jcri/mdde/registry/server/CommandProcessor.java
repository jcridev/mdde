package dev.jcri.mdde.registry.server;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.ICommandPreProcessor;
import dev.jcri.mdde.registry.control.ReadCommand;
import dev.jcri.mdde.registry.control.WriteCommand;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;

public class CommandProcessor {
    public CommandProcessor(IReadCommandHandler readHandler,
                            IWriteCommandHandler writeHandler,
                            ICommandPreProcessor<String, String> commandPreProcessor,
                            ICommandParser<String, ReadCommand, String> readCommandParser,
                            ICommandParser<String, WriteCommand, String> writeCommandParser){

    }
}
