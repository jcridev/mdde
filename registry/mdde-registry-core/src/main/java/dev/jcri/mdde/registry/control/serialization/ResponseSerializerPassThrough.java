package dev.jcri.mdde.registry.control.serialization;

import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistry;

import java.util.List;
import java.util.Set;

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
    public Object serialize(Set<String> value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(int value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(boolean value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(FullRegistry value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(BenchmarkRunResult value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(BenchmarkStatus value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serialize(FragmentCatalog value) throws ResponseSerializationException {
        return value;
    }

    @Override
    public Object serializeException(Throwable cause) {
        return cause;
    }
}
