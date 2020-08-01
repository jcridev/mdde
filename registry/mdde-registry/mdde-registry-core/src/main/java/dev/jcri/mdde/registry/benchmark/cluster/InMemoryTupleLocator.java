package dev.jcri.mdde.registry.benchmark.cluster;

import dev.jcri.mdde.registry.data.exceptions.KeyNotFoundException;
import dev.jcri.mdde.registry.shared.store.response.TupleCatalog;
import dev.jcri.mdde.registry.utility.MapTools;

import java.io.IOException;
import java.util.*;

/**
 * An in-memory snapshot of the current state of the tuple distribution.
 *
 * The primary purpose is not individual record retrieval efficiency but making sure that retrieval of a tuple is
 * performed with O(1) complexity even if it's not always the fastest retrieval.
 * We want to eliminate the possibility of the specifics of the registry implementation in benchmark metrics.
 */
public class InMemoryTupleLocator implements IReadOnlyTupleLocator {
    private final ICapacityCounter<String> _capacityCounter;

    public InMemoryTupleLocator(ICapacityCounter<String> capacityCounter){
        Objects.requireNonNull(capacityCounter, "Capacity counter object can't be null");
        _capacityCounter = capacityCounter;
    }

    /**
     * Simple binary array storing the state of the registry
     * Columns: Tuples
     * Rows: Nodes
     */
    private boolean[][] _registrySnapshot = null;
    /**
     * Nodes: Integer value is the internal ID (corresponds to the snapshot row number)
     */
    private final Map<Integer, String> _nodes = new HashMap<>();
    private Map<String, Integer> _nodesInverse = null;
    /**
     * Tuples: Integer value is the internal ID (corresponds to the snapshot column number)
     */
    private Map<String, Integer> _tuples = new HashMap<>();

    /**
     * Select the node from which the tuple should be read.
     * @param tupleId Tuple Id.
     * @return Node Id.
     */
    @Override
    public String getNodeForRead(String tupleId) {
        if(_registrySnapshot == null){
            throw new IllegalStateException("The data locator is not initialized");
        }

        var tupleIndex = getTupleIndexByIteratingThroughAllRecords(tupleId);
        if (tupleIndex == null) { // Tuple not found in this registry
            return null;
        }

        List<Integer> containingNodes = new ArrayList<>();
        for (int i = 0; i < _registrySnapshot.length; i++) {
            boolean[] nodeRow = _registrySnapshot[i];
            if(nodeRow[tupleIndex]){
                containingNodes.add(i);
            }
        }
        if(containingNodes.size() > 1){
            var consumedCapacity = _capacityCounter.read(containingNodes.toArray(new Integer[0]));
            var result = consumedCapacity[containingNodes.get(0)];
            var sIdx = containingNodes.get(0);
            for (int i = 0; i < containingNodes.size(); i++) {
                var nodeIdx = containingNodes.get(i);
                if (consumedCapacity[nodeIdx] < result) {
                    result = consumedCapacity[nodeIdx];
                    sIdx = nodeIdx;
                }
            }
            final var selectedNodeIdx = sIdx;
            _capacityCounter.increment(selectedNodeIdx);
            return _nodes.get(selectedNodeIdx);
        }
        else{
            final var selectedNodeIdx = containingNodes.get(0);
            _capacityCounter.increment(selectedNodeIdx);
            return _nodes.get(selectedNodeIdx);
        }
    }

    /**
     * Decrease the used capacity counter for the selected Node Id.
     * @param nodeId Node Id.
     * @throws KeyNotFoundException Node Id is not found in the records locator.
     */
    @Override
    public void notifyReadFinished(String nodeId) throws KeyNotFoundException {
        var nodeIdx = _nodesInverse.get(nodeId);
        if(nodeIdx == null){
            throw new KeyNotFoundException(nodeId);
        }
        _capacityCounter.decrement(nodeIdx);
    }

    /**
     * Initialize the instance of this data locator
     * @param tupleCatalog Current tuple distribution
     */
    @Override
    public void initializeDataLocator(TupleCatalog tupleCatalog) {
        Objects.requireNonNull(tupleCatalog);
        _nodes.clear();
        _tuples.clear();
        if(_nodesInverse != null) {
            _nodesInverse.clear();
        }
        _registrySnapshot = new boolean[tupleCatalog.getNodes().size()][tupleCatalog.getTuples().size()];
        // "Reindex" tuples, in case the catalog indexes are for some reason aren't sequential 0-N values
        int tupleIdx = 0;
        Map<Integer, Integer> tmpKeyTupleMapping = new HashMap<>();
        for (Map.Entry<Integer, String> tuple : tupleCatalog.getTuples().entrySet()) {
            _tuples.put(tuple.getValue(), tupleIdx);
            tmpKeyTupleMapping.put(tuple.getKey(), tupleIdx);
            tupleIdx++;
        }
        // "Reindex" nodes, in case the catalog indexes are for some reason aren't sequential 0-N values
        int nodeIdx = 0;
        Map<Integer, Integer> tmpKeyNodeMapping = new HashMap<>();
        for (Map.Entry<Integer, String> node : tupleCatalog.getNodes().entrySet()) {
            _nodes.put(nodeIdx, node.getValue());
            tmpKeyNodeMapping.put(node.getKey(), nodeIdx);
            nodeIdx++;
        }
        // Create a binary map for all node-tuple relationships
        for (Map.Entry<Integer, List<Integer>> entry : tupleCatalog.getNodeContents().entrySet()) {
            var rowIdx = tmpKeyNodeMapping.get(entry.getKey());
            for (var columnIdx : entry.getValue()) {
                _registrySnapshot[rowIdx][tmpKeyTupleMapping.get(columnIdx)] = true;
            }
        }
        _nodesInverse = MapTools.invert(_nodes);
        // Initialize capacity counter
        String[] nodeIndexes = new String[_nodes.size()];
        for (Map.Entry<Integer, String> node : tupleCatalog.getNodes().entrySet()){
            nodeIndexes[node.getKey()] = node.getValue();
        }

        _capacityCounter.initialize(Arrays.asList(nodeIndexes));
    }

    /**
     * Dumb linear iteration. Slow but it's guaranteed to be O(1).
     * @param tupleId ID of the tuple in question.
     * @return Integer internal ID of the tuple or null if not found.
     */
    private Integer getTupleIndexByIteratingThroughAllRecords(String tupleId){
        Integer result = null;
        // TODO: Optimize but preserve O(1) complexity
        for(var entry: _tuples.entrySet()){
            if(entry.getKey().equals(tupleId)){
                result = entry.getValue();
            }
        }
        return result;
    }

    /**
     * Not really necessary in this implementation as we don't open any external I/O resources, all is stored in RAM.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        // It's pointless in this implementation but Closable is part of the base interface for possible future
        // implementations
        _registrySnapshot = null; // Makes the runner "deinitialized"
        _nodes.clear();
        _tuples = null;
    }
}
