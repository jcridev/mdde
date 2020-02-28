package dev.jcri.mdde.registry.shared.store.response;

import dev.jcri.mdde.registry.utility.MapTools;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A container class for tuple allocation.
 */
public class TupleCatalog {
    private Map<Integer, String> _nodes;
    private Map<Integer, String> _tuples;
    private Map<Integer, List<Integer>> _nodeContent;

    public TupleCatalog(Map<String, Set<String>> nodesWithContents) throws InterruptedException {
        Objects.requireNonNull(nodesWithContents);
        // Collect unique tupleIds
        _tuples = new HashMap<>();
        Iterator<Set<String>> it = nodesWithContents.values().iterator();
        Set<String> uniqueTupleIds = new HashSet<>(it.next());
        while(it.hasNext()) {
            uniqueTupleIds.addAll(it.next());
        }
        int tId = 0;
        for(String uTupleId: uniqueTupleIds){
            _tuples.put(tId ++, uTupleId);
        }
        Map<String, Integer> tmpTuplesToId = MapTools.invert(_tuples);

        // Fill out nodes
        _nodes = new HashMap<>();
        _nodeContent = new HashMap<>();
        ExecutorService nodesFillerExecutor = Executors.newCachedThreadPool();
        int nId = 0;
        for(String uNodeId: nodesWithContents.keySet()){
            _nodes.put(nId, uNodeId);
            List<Integer> convertedIds =  new ArrayList<Integer>();
            _nodeContent.put(nId++, convertedIds);
            nodesFillerExecutor.execute(
                    new NodeContentFiller(nodesWithContents.get(uNodeId), convertedIds, tmpTuplesToId));
        }
        nodesFillerExecutor.shutdown();
        nodesFillerExecutor.awaitTermination(5, TimeUnit.MINUTES);
    }

    /**
     * Get names of the nodes as defined in the registry mapped to the temporary integer IDs.
     * @return
     */
    public Map<Integer, String> getNodes() {
        return _nodes;
    }

    /**
     * Get UIDs of the tuples as defined in the registry mapped to the temporary integer IDs.
     * @return
     */
    public Map<Integer, String> getTuples() {
        return _tuples;
    }

    /**
     * Get Full catalog of tuples located in the registry, both node IDs and tuple IDs are encoded to integers.
     * @return
     */
    public Map<Integer, List<Integer>> getNodeContents() {
        return _nodeContent;
    }


    private class NodeContentFiller implements Runnable {
        private final Set<String> _tupleIdsInTheNode;
        private final List<Integer> _localIds;
        private final Map<String, Integer> _tupleCatalog;

        /**
         * Constructor
         * @param tupleIds A set of tuple IDs located in the node
         * @param localTupleIds A collection that should be filled with the local unique tuple representation IDs
         * @param tupleCatalog Map of original tuple IDs to local integer IDs
         */
        public NodeContentFiller(Set<String> tupleIds, List<Integer> localTupleIds, Map<String, Integer> tupleCatalog){
            _tupleIdsInTheNode = tupleIds;
            _localIds = localTupleIds;
            _tupleCatalog = tupleCatalog;
        }

        public void run()
        {
            for(String tupleId: _tupleIdsInTheNode){
                _localIds.add(_tupleCatalog.get(tupleId));
            }
        }
    }
}
