package dev.jcri.mdde.registry.benchmark.cluster;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class CapacityCounter implements Closeable {
    // https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html
    // https://en.wikipedia.org/wiki/Boltzmann_distribution
    // http://tutorials.jenkov.com/java-performance/ring-buffer.html
    private final ConcurrentLinkedQueue<Boolean>[] _capacityCounters;

    /**
     * Constructor
     * @param numberOfNodes Number of nodes.
     */
    public CapacityCounter(int numberOfNodes){
        ConcurrentLinkedQueue<Boolean>[] counterQueues
                = (ConcurrentLinkedQueue<Boolean>[])new ConcurrentLinkedQueue<?>[numberOfNodes];
        for(int i = 0; i < numberOfNodes; i++){
            counterQueues[i] = new ConcurrentLinkedQueue<Boolean>();
        }
        _capacityCounters = counterQueues;
    }


    @Override
    public void close() throws IOException {

    }
}
