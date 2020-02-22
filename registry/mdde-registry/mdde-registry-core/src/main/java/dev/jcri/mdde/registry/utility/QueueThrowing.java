package dev.jcri.mdde.registry.utility;
import java.io.IOException;

public interface QueueThrowing<T> {
    int size() throws IOException;

    boolean isEmpty() throws IOException;

    boolean contains(Object o) throws IOException;

    IteratorThrowing<T> iterator();

    Object[] toArray() throws IOException;

    T[] toArray(T[] a) throws IOException;

    boolean add(T dataAction) throws IOException;

    void clear() throws IOException;

    T remove() throws IOException;

    T poll() throws IOException;

    T element() throws IOException;

    T peek() throws IOException;
}
