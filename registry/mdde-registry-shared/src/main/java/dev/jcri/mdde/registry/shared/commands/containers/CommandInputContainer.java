package dev.jcri.mdde.registry.shared.commands.containers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.jcri.mdde.registry.shared.commands.Constants;
import dev.jcri.mdde.registry.shared.commands.containers.utility.JacksonBase64ToStringSerializer;

/**
 * Expected structure of the command container (JSON, XML, etc).
 */
public class CommandInputContainer<TArgs> {
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
    private TArgs args;

    @JsonGetter(Constants.CommandFiled)
    public String getCmd() {
        return cmd;
    }
    @JsonSetter(Constants.CommandFiled)
    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @JsonGetter(Constants.ArgumentsField)
    public TArgs getArgs() {
        return args;
    }
    @JsonSetter(Constants.ArgumentsField)
    public void setArgs(TArgs arguments) {
        this.args = arguments;
    }
}
