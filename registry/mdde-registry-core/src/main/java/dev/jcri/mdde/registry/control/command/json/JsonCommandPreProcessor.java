package dev.jcri.mdde.registry.control.command.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jcri.mdde.registry.control.ICommandPreProcessor;
import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.control.exceptions.MalformedCommandStatementException;

/**
 * Incoming statement processor. Splits the statement into the command tag string and JSON string containing arguments
 */
public class JsonCommandPreProcessor implements ICommandPreProcessor<String, String> {
    public static final String COMMAND_FIELD = "cmd";
    public static final String ARGUMENTS_FIELD = "args";

    @Override
    public CommandComponents<String> splitIncoming(String statement) throws MalformedCommandStatementException {
        ObjectMapper mapper = new ObjectMapper();
        String command = null;
        String arguments = null;
        try {
            JsonNode rootNode = mapper.readTree(statement);
            command = rootNode.get(COMMAND_FIELD).asText();
            arguments = rootNode.get(ARGUMENTS_FIELD).toString();
        } catch (JsonProcessingException e) {
            var expectedJson = String.format("Expected JSON: '{\"%s\":\"COMMAND_KEYWORD\", \"%s\":{...}}'",
                    COMMAND_FIELD, ARGUMENTS_FIELD);
            throw new MalformedCommandStatementException(expectedJson, e);
        }

        return new CommandComponents<>(command, arguments);
    }
}
