package dev.jcri.mdde.registry.store.response;

import java.beans.Transient;
import java.io.Serializable;
import java.util.*;

public class FullRegistry implements Serializable {
    /**
     * Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
     */
    private final Map<String, Map<String, List<String>>> _registry;

    /**
     * Construct registry
     * @param registry Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
     */
    public FullRegistry(Map<String, Map<String, List<String>>> registry){
        Objects.requireNonNull(registry, "Registry map can't be null");
        _registry = registry;
    }

    public Map<String, Map<String, List<String>>> getRegistry() {
        return _registry;
    }

    @Transient
    public Set<String> getNodes(){
        return _registry.keySet();
    }

    @Transient
    public Map<String, List<String>> getNodeContents(String nodeId){
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID must be supplied");
        }

        return  _registry.get(nodeId);
    }

    @Transient
    public Set<String> getNodeFragments(String nodeId){
        var contents = getNodeContents(nodeId);
        if(contents == null){
            return new HashSet<String>(); // return empty value
        }
        return contents.keySet();
    }

}
