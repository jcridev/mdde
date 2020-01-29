package dev.jcri.mdde.registry.control.serialization;

import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistry;

import java.util.List;
import java.util.Set;

/**
 * Implement to create the registry query response serializer
 * @param <T>
 */
public interface IResponseSerializer<T> extends IResponseExceptionSerializer<T> {
    T serialize(String value) throws ResponseSerializationException;
    T serialize(List<String> value) throws ResponseSerializationException;
    T serialize(Set<String> value) throws ResponseSerializationException;
    T serialize(int value) throws ResponseSerializationException;
    T serialize(boolean value) throws ResponseSerializationException;
    T serialize(FullRegistry value) throws ResponseSerializationException;
    T serialize(BenchmarkRunResult value) throws ResponseSerializationException;
    T serialize(BenchmarkStatus value) throws ResponseSerializationException;
}
