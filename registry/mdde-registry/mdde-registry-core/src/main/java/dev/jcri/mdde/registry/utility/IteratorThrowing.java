package dev.jcri.mdde.registry.utility;

import java.io.IOException;

public interface IteratorThrowing<T> {
    boolean hasNext() throws IOException;
    T next() throws IOException;
}
