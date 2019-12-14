package dev.jcri.mdde.registry.store.response.serialization;

import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.store.response.FullRegistry;

import java.util.List;

/**
 * Implement to create the registry query response serializer
 * @param <T>
 */
public interface IResponseSerializer<T> {
    T serialize(String value) throws ResponseSerializationException;
    T serialize(List<String> value) throws ResponseSerializationException;
    T serialize(int value) throws ResponseSerializationException;
    T serialize(Object value) throws ResponseSerializationException;
    T serialize(FullRegistry value) throws ResponseSerializationException;
}
