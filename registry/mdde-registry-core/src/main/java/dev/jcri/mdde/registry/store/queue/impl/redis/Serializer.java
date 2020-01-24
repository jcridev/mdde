package dev.jcri.mdde.registry.store.queue.impl.redis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jcri.mdde.registry.store.queue.actions.DataAction;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;
import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class Serializer {

    private static ObjectMapper _mapperObject;
    private static ReentrantLock _mapperLock  = new ReentrantLock();
    private static ObjectMapper getMapper(){
        if(_mapperObject != null){
            return _mapperObject;
        }
        _mapperLock.lock();
        try{
            if(_mapperObject != null){
                return _mapperObject;
            }
            var newMapper = new ObjectMapper();
            newMapper.addMixIn(DataAction.class, TagIgnoreMixin.class);
            _mapperObject = newMapper;
            return _mapperObject;
        }
        finally {
            _mapperLock.unlock();
        }
    }


    public static String serialize(DataAction action) throws IOException {
        switch (action.getActionType()){
            case DELETE:
                return serializeDelete(action);
            case COPY:
                return serializeCopy(action);
            default:
                if(action != null){
                    throw new IllegalArgumentException(
                            String.format("Unknown data action %s", action.getActionType().getTag()));
                }
                else{
                    throw new IllegalArgumentException("null DataAction type tag");
                }
        }
    }

    public static DataAction deserialize(String serializedValue) throws IOException{
        var opCode = parseOperationCode(serializedValue);
        if(opCode == null){
            throw new IllegalArgumentException("Unable to parse operation code from the serialized value");
        }
        var json = extractJsonString(serializedValue);
        switch (opCode){
            case DELETE:
                return deserializeDelete(json);
            case COPY:
                return deserializeCopy(json);
            default:
                throw new IllegalArgumentException(
                    String.format("Unknown data action %s", opCode.getTag()));
        }
    }

    private static String addOperationCode(DataAction action, String json){
        return String.format("%03d%s", action.getActionType().getCode() ,json);
    }

    private static String extractJsonString(String serializedValue){
        return serializedValue.substring(3);
    }

    private static DataAction.EActionType parseOperationCode(String serializedValue){
        var opCodeString = serializedValue.substring(0, 3);
        var opCode = Byte.parseByte(opCodeString);
        return Arrays.stream(DataAction.EActionType.values())
                                .filter(value -> value.getCode() == opCode)
                                .findFirst()
                                .orElse(null);
    }

    private static DataCopyAction deserializeCopy(String json) throws JsonProcessingException {
        return getMapper().readValue(json, DataCopyActionJson.class);
    }

    private static DataDeleteAction deserializeDelete(String json) throws JsonProcessingException {
        return getMapper().readValue(json, DataDeleteActionJson.class);
    }

    private static String serializeCopy(DataAction action) throws JsonProcessingException {
        var copyAction = (DataCopyAction)action;
        var json = getMapper().writeValueAsString(copyAction);
        return addOperationCode(action, json);
    }

    private static String serializeDelete(DataAction action) throws JsonProcessingException {
        var delAction = (DataDeleteAction)action;
        var json = getMapper().writeValueAsString(delAction);
        return addOperationCode(action, json);
    }

    abstract class TagIgnoreMixin {
        @JsonIgnore
        public abstract DataAction.EActionType getActionType();
    }
}
