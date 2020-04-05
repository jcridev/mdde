package dev.jcri.mdde.registry.shared.commands.containers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jcri.mdde.registry.shared.commands.Constants;
import dev.jcri.mdde.registry.shared.commands.ICommand;

/**
 * Serialization helper for MDDE registry command input - output
 */
public class CommandSerializationHelper {
    public static <TArgs> String serializeJson(ICommand command, TArgs args)
            throws JsonProcessingException {
        ObjectMapper mapper  = new ObjectMapper();
        CommandInputContainer<TArgs> newInCmd = new CommandInputContainer<TArgs>();
        newInCmd.setCmd(command.getCommand());
        newInCmd.setArgs(args);
        return mapper.writeValueAsString(newInCmd);
    }

    public static <TRes> CommandResultContainer<TRes> deserializeJson(String response)
            throws JsonProcessingException {
        ObjectMapper mapper  = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response);
        String resultPayload = rootNode.get(Constants.ResultPayload).toString();
        String resultError = rootNode.get(Constants.ResultError).asText();

        TRes payload = null;

        if(resultPayload != null && !resultPayload.isEmpty()){
            payload = mapper.readValue(resultPayload, new TypeReference<TRes>() {});
        }

        CommandResultContainer<TRes> result = new CommandResultContainer<TRes>();
        result.setError(resultError);
        result.setResult(payload);

        return result;
    }

    /**
     * Deserialize custom classes
     * @param target Target payload class
     * @param response Response JSON string
     * @param <TRes> Target payload class parameter
     * @return
     * @throws JsonProcessingException
     */
    @SuppressWarnings("unchecked")
    public static <TRes> CommandResultContainer<TRes> deserializeJson(Class<?> target, String response)
            throws JsonProcessingException {
        ObjectMapper mapper  = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response);
        String resultPayload = rootNode.get(Constants.ResultPayload).toString();
        String resultError = rootNode.get(Constants.ResultError).asText();

        TRes payload = null;
        if(resultPayload != null && !resultPayload.isEmpty()){
            payload = (TRes) mapper.readValue(resultPayload, target);
        }

        CommandResultContainer<TRes> result = new CommandResultContainer<TRes>();
        result.setError(resultError);
        result.setResult(payload);

        return result;
    }
}
