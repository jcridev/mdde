package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandPreProcessor;
import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.control.command.sequential.arguments.ISequenceParser;
import dev.jcri.mdde.registry.control.command.sequential.arguments.SimpleSequenceParser;

/**
 * Pre-process for the command statements incoming as string lines.
 */
public class TextSequentialCommandPreProcessor implements ICommandPreProcessor<String, String> {
    private final ISequenceParser _stringParser = new SimpleSequenceParser();

    @Override
    public CommandComponents<String> splitIncoming(String statement) {
        return _stringParser.getArgumentsFromLine(statement);
    }
}
