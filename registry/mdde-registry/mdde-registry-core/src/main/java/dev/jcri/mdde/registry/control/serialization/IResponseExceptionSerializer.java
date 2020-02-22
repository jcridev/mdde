package dev.jcri.mdde.registry.control.serialization;

public interface IResponseExceptionSerializer<T> {
    /**
     * Used to report an error to the client in a serialized manner.
     * @param cause Throwable instance
     * @return Serialized value
     */
    T serializeException(Throwable cause);
}