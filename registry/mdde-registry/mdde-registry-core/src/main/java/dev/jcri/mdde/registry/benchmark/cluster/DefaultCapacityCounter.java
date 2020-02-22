package dev.jcri.mdde.registry.benchmark.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * Capacity counter simply using atomic counter per node
 * @param <TNodeId>
 */
public final class DefaultCapacityCounter<TNodeId> implements ICapacityCounter<TNodeId> {
    private final ArrayList<TNodeId> _idMapping = new ArrayList<>();
    // https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/LongAdder.html
    private final ArrayList<LongAdder> _capacityCounters = new ArrayList<>();

    @Override
    public void close() throws IOException {
        // No resources to dispose of in this implementation
    }

    @Override
    public void initialize(List<TNodeId> orderedNodes) {
        // Clear out existing
        _idMapping.clear();
        _capacityCounters.clear();
        // Fill in
        _idMapping.addAll(orderedNodes);
        for(var node: _idMapping){
            _capacityCounters.add(new LongAdder());
        }
    }

    @Override
    public TNodeId[] getIndexed() {
        return (TNodeId[]) _idMapping.toArray();
    }

    @Override
    public void increment(int nodeIndex) {
        _capacityCounters.get(nodeIndex).increment();
    }

    @Override
    public void decrement(int nodeIndex) {
        _capacityCounters.get(nodeIndex).decrement();
    }

    @Override
    public Integer[] read() {
        var result = new Integer[_idMapping.size()];
        for (int i = 0; i < _capacityCounters.size(); i++) {
            LongAdder counter = _capacityCounters.get(i);
            result[i] = counter.intValue();
        }
        return result;
    }

    @Override
    public Integer[] read(Integer... nodeIndex) {
        var result = new Integer[_idMapping.size()];
        Arrays.fill(result, -1);
        for(var requestedIndex: nodeIndex){
            LongAdder counter = _capacityCounters.get(requestedIndex);
            result[requestedIndex] = counter.intValue();
        }
        return result;
    }
}
