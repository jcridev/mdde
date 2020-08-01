package dev.jcri.mdde.registry.control.serialization;

/**
 * Response serializer for the command handler interface.
 * @param <T> Type of response object.
 */
public interface IResponseExceptionSerializer<T> {
    /**
     * Used to report an error to the client in a serialized manner.
     * @param cause Throwable instance.
     * @return Serialized value.
     */
    T serializeException(Throwable cause);
}