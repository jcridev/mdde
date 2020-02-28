package dev.jcri.mdde.registry.store.impl;

import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.exceptions.RegistryEntityType;
import dev.jcri.mdde.registry.store.exceptions.UnknownEntityIdException;
import dev.jcri.mdde.registry.shared.store.response.FullRegistryAllocation;
import dev.jcri.mdde.registry.shared.store.response.TupleCatalog;

import java.util.*;

public abstract class ReadCommandHandler implements IReadCommandHandler {

//region Public reader APIs
    /**
     * Get the full registry state
     * @return Full registry state
     * @throws ReadOperationException
     */
    public FullRegistryAllocation getFullRegistry() throws ReadOperationException{
        return runGetFullRegistry();
    }

    @Override
    public TupleCatalog getTupleCatalog() throws ReadOperationException {
        var nodeAndContent = new HashMap<String, Set<String>>();

        var nodes = runGetNodes();
        for(String nodeId: nodes){
            var nodeTuples = runGetAllNodeTuples(nodeId);
            nodeAndContent.put(nodeId, nodeTuples);
        }
        try {
            return new TupleCatalog(nodeAndContent);
        } catch (InterruptedException e) {
            throw new ReadOperationException(e);
        }
    }

    @Override
    public FragmentCatalog getFragmentCatalog(Set<String> metaTagsExemplar, Set<String> metaTagsGlobal) {
        return runGetFragmentCatalog(metaTagsExemplar, metaTagsGlobal);
    }

    /**
     * Get node IDs where the tuple is located
     * @param tupleId Tuple ID
     * @return
     */
    public Set<String> getTupleNodes(final String tupleId){
        if(tupleId == null || tupleId.isEmpty()){
            throw new IllegalArgumentException("Tuple ID can't be null or empty");
        }
        return runGetTupleNodes(tupleId);
    }

    /**
     * Get fragment ID to which the tuple belongs, if unassigned, null is returned instead
     * @param tupleId Tuple ID
     * @return
     */
    public String getTupleFragment(final String tupleId){
        if(tupleId == null || tupleId.isEmpty()){
            throw new IllegalArgumentException("Tuple ID can't be null or empty");
        }
        return runGetTupleFragment(tupleId);
    }

    /**
     * Get node IDs where the fragment is located
     * @param fragmentId Fragment ID
     * @return
     */
    public Set<String> getFragmentNodes(final String fragmentId) {
        if(fragmentId == null || fragmentId.isEmpty()){
            throw new IllegalArgumentException("Fragment ID can't be null or empty");
        }
        return runGetFragmentNodes(fragmentId);
    }

    /**
     * Get all fragment IDs located on the node
     * @param nodeId Node ID
     * @return
     */
    public Set<String> getNodeFragments(final String nodeId){
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID can't be null or empty");
        }
        return runGetNodeFragments(nodeId);
    }

    /**
     * Get tuple IDs assigned to the fragment
     * @param fragmentId Fragment ID
     * @return
     * @throws ReadOperationException
     */
    public Set<String> getFragmentTuples(final String fragmentId)
            throws ReadOperationException {
        if(fragmentId == null || fragmentId.isEmpty()){
            throw new IllegalArgumentException("Fragment ID can't be null or empty");
        }
        return runGetFragmentTuples(fragmentId);
    }

    /**
     * Get the number of instances of the specific fragment, that are tracked by the registry
     * @param fragmentId Fragment ID
     * @return
     */
    public int getCountFragment(final String fragmentId) {
        if(fragmentId == null || fragmentId.isEmpty()){
            throw new IllegalArgumentException("Fragment ID can't be null or empty");
        }
        return runGetCountFragment(fragmentId);
    }

    /**
     * Get the number of instances of the specific tuple, that are tracked by the registry
     * @param tupleId Tuple ID
     * @return
     */
    public int getCountTuple(final String tupleId){
        if(tupleId == null || tupleId.isEmpty()){
            throw new IllegalArgumentException("Tuple ID can't be null or empty");
        }
        return runGetCountTuple(tupleId);
    }

    /**
     * Get a list of all node IDs that are part of this registry
     * @return
     */
    public Set<String> getNodes(){
        return runGetNodes();
    }

    /**
     * Check if node exists in the registry
     * @param nodeId Node ID
     * @return True - node is found, False otherwise
     */
    public boolean getIsNodeExists(final String nodeId){
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID can't be null or empty");
        }
        return runGetIsNodeExists(nodeId);
    }

    /**
     * Check if tuple exists in the registry
     * @param tupleId Tuple ID
     * @return
     */
    public boolean getIsTupleExists(final String tupleId){
        if(tupleId == null || tupleId.isEmpty()){
            throw new IllegalArgumentException("Tuple ID can't be null or empty");
        }
        return runGetIsTupleExists(tupleId);
    }

    /**
     * Check if fragment exist
     * @param fragmentId Fragment ID
     * @return
     */
    public boolean getIsFragmentExists(final String fragmentId){
        if(fragmentId == null || fragmentId.isEmpty()){
            throw new IllegalArgumentException("Fragment ID can't be null or empty");
        }
        return runGetIsFragmentExists(fragmentId);
    }

    /**
     * Get tuples that were not yet assigned to any fragment on a specific node
     * @param nodeId Node ID
     * @return
     */
    public Set<String> getUnassignedTuples(String nodeId){
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID can't be null or empty");
        }
        return runGetUnassignedTuples(nodeId);
    }

    /**
     * Check if a specific tuple is in the unassigned heap on a specific node
     * @param nodeId Node ID
     * @param tupleIds Tuple ID
     * @return
     */
    public Boolean getIsTuplesUnassigned(String nodeId, Set<String> tupleIds){
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID can't be null or empty");
        }
        if(tupleIds == null || tupleIds.size() == 0){
            throw new IllegalArgumentException("Tuple IDs set can't be null or empty");
        }
        return runGetIsTuplesUnassigned(nodeId, tupleIds);
    }

    /**
     * Check of a specific node is stored on a specific node
     * @param nodeId Node ID
     * @param fragmentId Fragment ID
     * @return
     */
    public Boolean getIsNodeContainsFragment(String nodeId, String fragmentId)
            throws UnknownEntityIdException {
        if(fragmentId == null || fragmentId.isEmpty()){
            throw new IllegalArgumentException("Fragment ID can't be null or empty");
        }
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID can't be null or empty");
        }

        if(!getIsNodeExists(nodeId)){
            throw new UnknownEntityIdException(RegistryEntityType.Node, nodeId);
        }

        return runGetIsNodeContainsFragment(nodeId, fragmentId);
    }

    /**
     * Get all fragment IDs registered within the registry
     * @return
     */
    public Set<String> getAllFragmentIds(){
        return runGetAllFragmentIds();
    }

    public String getMetaFragmentGlobal(String fragmentId, String metaName){
        if(fragmentId == null || fragmentId.isEmpty()){
            throw new IllegalArgumentException("Fragment ID can't be null or empty");
        }
        if(metaName == null || metaName.isEmpty()){
            throw new IllegalArgumentException("Meta field name can't be null or empty");
        }

        return runGetGlobalFragmentMeta(fragmentId, metaName);
    }

    public String getMetaFragmentExemplar(String fragmentId, String nodeId, String metaName){
        if(fragmentId == null || fragmentId.isEmpty()){
            throw new IllegalArgumentException("Fragment ID can't be null or empty");
        }
        if(metaName == null || metaName.isEmpty()){
            throw new IllegalArgumentException("Meta field name can't be null or empty");
        }
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID can't be null or empty");
        }

        return runGetExemplarFragmentMeta(fragmentId, nodeId, metaName);
    }
//endregion
//region Abstract methods
    /**
     * Retrieve a snapshot of the current state for the entire registry including tuples, nodes and fragments
     * @return Current registry snapshot
     */
    protected abstract FullRegistryAllocation runGetFullRegistry() throws ReadOperationException;

    /**
     * Get the list of node Ids where the tuple is located
     * @param tupleId Tuple ID
     * @return list of nodes where the specific tuple can be found
     */
    protected abstract Set<String> runGetTupleNodes(final String tupleId);

    /**
     * Get the id of a fragment to which the tuple belongs
     * @param tupleId Tuple ID
     * @return Fragment ID
     */
    protected abstract String runGetTupleFragment(final String tupleId);

    /**
     * Get the list of node IDs where the fragment is located
     * @param fragmentId Fragment ID
     * @return List of Node IDs
     */
    protected abstract Set<String> runGetFragmentNodes(final String fragmentId);

    /**
     * Get all of the fragments stored within the node
     * @param nodeId Node ID
     * @return Set of fragments mapped to the node
     */
    protected abstract Set<String> runGetNodeFragments(final String nodeId);

    /**
     * Get the list of all tuple IDs that belong to the fragment
     * @param fragmentId Fragment ID
     * @return List of Tuple IDs
     */
    protected abstract Set<String> runGetFragmentTuples(final String fragmentId) throws ReadOperationException;

    /**
     * Get the number of instances of a specific fragment
     * @param fragmentId Fragment ID
     * @return Count of the specified fragment instances across all nodes
     */
    protected abstract int runGetCountFragment(final String fragmentId);

    /**
     * Get the number of instances of a specific tuple
     * @param tupleId Tuple ID
     * @return Count of the specified tuple instances across all nodes
     */
    protected abstract int runGetCountTuple(final String tupleId);

    /**
     * Get the list of all node IDs
     * @return List of all node IDs that are present in the registry
     */
    protected abstract Set<String> runGetNodes();

    /**
     * Check if the specified node ID is registered within the registry
     * @param nodeId Node ID
     * @return True if node exists, False otherwise
     */
    protected abstract boolean runGetIsNodeExists(final String nodeId);
    /**
     * Check if the specified tuple ID is registered within the registry
     * @param tupleId Tuple ID
     * @return True if tuple exists, False otherwise
     */
    protected boolean runGetIsTupleExists(final String tupleId){
        return runGetCountFragment(tupleId) > 0;
    }
    /**
     * Check if the specified fragment ID is registered within the registry
     * @param fragmentId Fragment ID
     * @return True if fragment exists, False otherwise
     */
    protected boolean runGetIsFragmentExists(final String fragmentId){
        return runGetCountFragment(fragmentId) > 0;
    }

    /**
     * Get a set of tuples not assigned to a fragment, located on a specific node
     * @param nodeId Node ID
     * @return Set of Tuple IDs
     */
    protected abstract Set<String> runGetUnassignedTuples(String nodeId);

    /**
     * Check of the specific tuple is unassigned on a specific node
     * @param nodeId Tuple ID
     * @param tupleIds Node ID
     * @return True - tuple is unassigned
     */
    protected abstract Boolean runGetIsTuplesUnassigned(String nodeId, Set<String> tupleIds);

    /**
     * Check if node contains fragment
     * @param nodeId Node ID
     * @param fragmentId Fragment ID
     * @return True - specified fragment is located within the specified node
     */
    protected abstract Boolean runGetIsNodeContainsFragment(String nodeId, String fragmentId);

    /**
     * Get a set of all fragments IDs
     * @return All registered fragment IDs
     */
    protected abstract Set<String> runGetAllFragmentIds();

    /**
     * Get a set of all tuple IDs that assigned to this Node
     * @return All tuples located on the node, both assigned to fragments and not.
     */
    protected abstract Set<String> runGetAllNodeTuples(String nodeId) throws ReadOperationException;

    protected abstract String runGetGlobalFragmentMeta(String fragmentId, String metaName);

    protected abstract String runGetExemplarFragmentMeta(String fragmentId, String nodeId, String metaName);

    protected abstract FragmentCatalog runGetFragmentCatalog(Set<String> metaTagsExemplar, Set<String> metaTagsGlobal);
//endregion
}
