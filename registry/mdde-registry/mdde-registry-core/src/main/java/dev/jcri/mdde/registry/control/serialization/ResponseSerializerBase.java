package dev.jcri.mdde.registry.control.serialization;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistryAllocation;

import java.util.List;
import java.util.Set;

/**
 * Implement to create the registry query response serializer
 * @param <T>
 */
public abstract class ResponseSerializerBase<T> implements IResponseExceptionSerializer<T> {
    public abstract T serialize(String value) throws ResponseSerializationException;
    public abstract T serialize(List<String> value) throws ResponseSerializationException;
    public abstract T serialize(Set<String> value) throws ResponseSerializationException;
    public abstract T serialize(int value) throws ResponseSerializationException;
    public abstract T serialize(boolean value) throws ResponseSerializationException;
    public abstract T serialize(FullRegistryAllocation value) throws ResponseSerializationException;
    public abstract T serialize(BenchmarkRunResult value) throws ResponseSerializationException;
    public abstract T serialize(BenchmarkStatus value) throws ResponseSerializationException;
    public abstract T serialize(FragmentCatalog value) throws ResponseSerializationException;

    @Override
    public final T serializeException(Throwable cause){
        if(cause == null){
            return serializeErrorWithCode(EErrorCode.UNSPECIFIED_ERROR, "Unspecified error (please debug the registry)");
        }

        EErrorCode code = EErrorCode.RUNTIME_ERROR;
        // If it's one of the internal MDDE registry exceptions, get the custom code
        if(cause instanceof MddeRegistryException){
            var mddeCause = (MddeRegistryException) cause;
            code = mddeCause.getErrorCode();
        }
        // If the error message is empty can't be retrieved, replace it with the class name
        var errorMessage = cause.getMessage();
        if(errorMessage == null || errorMessage.isEmpty()){
            errorMessage = cause.getClass().getName();
        }

        return serializeErrorWithCode(code, errorMessage);
    }

    protected abstract T serializeErrorWithCode(EErrorCode errorCode, String message);
}
