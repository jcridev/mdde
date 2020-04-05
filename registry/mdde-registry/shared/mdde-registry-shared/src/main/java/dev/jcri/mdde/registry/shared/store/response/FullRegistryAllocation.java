package dev.jcri.mdde.registry.shared.store.response;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.beans.Transient;
import java.util.*;

/**
 * Allocation information for all of the fragments and tuples in the registry
 */
public class FullRegistryAllocation {
    private static final String REGISTRY_STATE_FIELD = "registry";
    /**
     * Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
     */
    private Map<String, Map<String, Set<String>>> _registry;

    /**
     * Construct registry
     * @param registry Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
     */
    public FullRegistryAllocation(Map<String, Map<String, Set<String>>> registry){
        Objects.requireNonNull(registry, "Registry map can't be null");
        _registry = registry;
    }

    /**
     * Default constructor
     */
    public FullRegistryAllocation(){ _registry = null;};

    /**
     * Get the map containing the entirety of the registry contents
     * @return Dictionary <Node ID, Dictionary <Fragment ID, Set <Tuple ID>>>
     */
    @JsonGetter(REGISTRY_STATE_FIELD)
    public Map<String, Map<String, Set<String>>> getRegistry() {
        return _registry;
    }
    @JsonSetter(REGISTRY_STATE_FIELD)
    public void setRegistry(Map<String, Map<String, Set<String>>> registry) {
        _registry = registry;
    }

    /**
     * Get the list of nodes
     * @return Set of Node IDs in the registry
     */
    @Transient
    @JsonIgnore
    public Set<String> getNodes(){
        return _registry.keySet();
    }

    /**
     * Get fragments stored on the specified node with tuple contents mapped to it
     * @param nodeId Node ID
     * @return Dictionary <Fragment ID, Set<Tuple ID>>
     */
    @Transient
    @JsonIgnore
    public Map<String, Set<String>> getNodeContents(String nodeId){
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID must be supplied");
        }

        return  _registry.get(nodeId);
    }

    /**
     * Get ids of the fragments stored on the specified node
     * @param nodeId Node ID
     * @return Set of fragment IDs
     */
    @Transient
    @JsonIgnore
    public Set<String> getNodeFragments(String nodeId){
        Map<String, Set<String>> contents = getNodeContents(nodeId);
        if(contents == null){
            return new HashSet<>(); // return empty value
        }
        return contents.keySet();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FullRegistryAllocation)){
            return false;
        }
        FullRegistryAllocation objB = (FullRegistryAllocation) obj;
        if(this.getRegistry() == null && objB.getRegistry() == null){
            return true;
        }

        if(this.getRegistry().size() != objB.getRegistry().size()){
            return false;
        }

        for(Map.Entry<String, Map<String, Set<String>>> node: getRegistry().entrySet()){
            if(!objB.getRegistry().containsKey(node.getKey())){
                return false;
            }
            boolean equals = node.getValue().entrySet().stream()
                    .allMatch(e -> e.getValue()
                                    .equals(objB.getRegistry()
                                                .get(node.getKey())
                                                .get(e.getKey())
                                    ));

            if(!equals){
                return false;
            }
        }

        return true;
    }
}
