package dev.jcri.mdde.registry.benchmark.cluster;

import dev.jcri.mdde.registry.store.response.TupleCatalog;
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

    /**
     * Simple binary array storing the state of the registry
     * Columns: Tuples
     * Rows: Nodes
     */
    private boolean[][] _registrySnapshot = null;
    /**
     * Nodes: Integer value is the internal ID (corresponds to the snapshot row number)
     */
    private Map<Integer, String> _nodes=null;
    /**
     * Tuples: Integer value is the internal ID (corresponds to the snapshot column number)
     */
    private Map<String, Integer> _tuples=null;
    private Random randomNodeGen = new Random();
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

        // TODO: Implement "capacity" logic instead of a Random node return
        var randomIndex = randomNodeGen.nextInt(containingNodes.size());
        return _nodes.get(containingNodes.get(randomIndex));
    }

    /**
     * Initialize the instance of this data locator
     * @param tupleCatalog Current tuple distribution
     */
    @Override
    public void initializeDataLocator(TupleCatalog tupleCatalog) {
        Objects.requireNonNull(tupleCatalog);


        _registrySnapshot = new boolean[tupleCatalog.getNodes().size()][tupleCatalog.getTuples().size()];
        _nodes = tupleCatalog.getNodes();
        _tuples = MapTools.invert(tupleCatalog.getTuples());
    }

    /**
     * Dumb linear iteration. Slow but it's guaranteed to be O(1)
     * @param tupleId ID of the tuple in question
     * @return Integer internal ID of the tuple or null if not found
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
     * Not really necessary in this implementation
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        // It's pointless in this implementation but Closable is part of the base interface for possible future
        // implementations
        _registrySnapshot = null; // Makes the runner "deinitialized"
        _nodes = null;
        _tuples = null;
    }
}
