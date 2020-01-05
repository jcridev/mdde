package dev.jcri.mdde.registry.control.command.json;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.jcri.mdde.registry.control.ICommandPreProcessor;
import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.control.command.json.extensions.JacksonKeepAsStringDeserializer;
import dev.jcri.mdde.registry.control.exceptions.MalformedCommandStatementException;
import dev.jcri.mdde.registry.shared.commands.Constants;

public class JsonCommandPreProcessor implements ICommandPreProcessor<String, String> {
    @Override
    public CommandComponents<String> splitIncoming(String statement) throws MalformedCommandStatementException {
        ObjectMapper mapper = new ObjectMapper();

        IncomingStatement parsedStatement = null;
        try {
            parsedStatement = mapper.readValue(statement, IncomingStatement.class);
        } catch (JsonProcessingException e) {
            var expectedJson = "Expected JSON: '{\"cmd\":\"COMMAND_KEYWORD\", \"args\":{...}}'";
            throw new MalformedCommandStatementException(expectedJson, e);
        }

        return new CommandComponents<>(parsedStatement.cmd, parsedStatement.args);
    }

    /**
     * Expected base structure of the incoming JSON
     */
    public static class IncomingStatement{
        /**
         * Keyword
         */
        @JsonProperty(Constants.CommandFiled)
        private String cmd;

        /**
         * Arguments
         */
        @JsonRawValue
        @JsonDeserialize(using = JacksonKeepAsStringDeserializer.class)
        @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
        @JsonProperty(Constants.ArgumentsField)
        private String args;
        @JsonGetter(Constants.CommandFiled)
        public String getCmd() {
            return cmd;
        }
        @JsonSetter(Constants.CommandFiled)
        public void setCmd(String cmd) {
            this.cmd = cmd;
        }
        @JsonGetter(Constants.ArgumentsField)
        public String getArgs() {
            return args;
        }
        @JsonSetter(Constants.ArgumentsField)
        public void setArgs(String arguments) {
            this.args = arguments;
        }
    }
}
