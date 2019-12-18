package dev.jcri.mdde.registry.control.sequetial.arguments;

import dev.jcri.mdde.registry.control.ReadCommands;
import dev.jcri.mdde.registry.control.WriteCommands;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestSimpleCommandParser {
    @Test
    public void testKeywordParsing(){
        var simpleParser = new SimpleSequenceParser();

        // Existence
        assertEquals(WriteCommands.INSERT_TUPLE,
                simpleParser.tryGetIsWriteCommandKeyword(MessageFormat.format("{0} \"some random stuff\"", WriteCommands.INSERT_TUPLE.getCommand())));

        assertEquals(ReadCommands.FIND_TUPLE,
                simpleParser.tryGetIsReadCommandKeyword(MessageFormat.format("{0} \"some random stuff\"", ReadCommands.FIND_TUPLE.getCommand())));

        // Non existence
        assertNull(simpleParser.tryGetIsWriteCommandKeyword("NON_EXISTING_COMMAND"));
        assertNull(simpleParser.tryGetIsReadCommandKeyword("NON_EXISTING_COMMAND"));
    }
}
