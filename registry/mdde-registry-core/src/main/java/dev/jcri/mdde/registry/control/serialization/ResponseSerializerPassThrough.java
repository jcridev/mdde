package dev.jcri.mdde.registry.control.serialization;

import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.store.response.FullRegistry;

import java.util.List;

/**
 * Not a serializer really, just returns the output as a Object type.
 * Intended for unit testing and debugging, hardly makes sense in production.
 */
public class ResponseSerializerPassThrough implements IResponseSerializer<Object> {
    @Override
    public Object serialize(String value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(List<String> value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(int value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(Object value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(FullRegistry value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serializeException(Throwable cause) {
        return cause;
    }
}
