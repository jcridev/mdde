package dev.jcri.mdde.registry.shared.commands.containers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.jcri.mdde.registry.shared.commands.Constants;

/**
 * Expected structure of the command container (JSON, XML, etc).
 */
public class CommandInputContainer {
    /**
     * Command tag (ex. 'GETALL')
     */
    @JsonProperty(Constants.CommandFiled)
    private String cmd;
    /**
     * Command arguments, if there are any expected. Must be serialized to string in the format which is
     * expected in the de-serializer (ICommandPreProcessor and ICommandParser)
     */
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
