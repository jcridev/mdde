package dev.jcri.mdde.registry.control.command.sequential.arguments;

import dev.jcri.mdde.registry.control.ReadCommand;
import dev.jcri.mdde.registry.control.WriteCommand;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestSimpleICommandParser {
    @Test
    public void testKeywordParsing(){
        var simpleParser = new SimpleSequenceParser();

        // Existence
        assertEquals(WriteCommand.INSERT_TUPLE,
                simpleParser.tryGetIsWriteCommandKeyword(MessageFormat.format("{0} \"some random stuff\"", WriteCommand.INSERT_TUPLE.getCommand())));

        assertEquals(ReadCommand.FIND_TUPLE,
                simpleParser.tryGetIsReadCommandKeyword(MessageFormat.format("{0} \"some random stuff\"", ReadCommand.FIND_TUPLE.getCommand())));

        // Non existence
        assertNull(simpleParser.tryGetIsWriteCommandKeyword("NON_EXISTING_COMMAND"));
        assertNull(simpleParser.tryGetIsReadCommandKeyword("NON_EXISTING_COMMAND"));
    }
}
