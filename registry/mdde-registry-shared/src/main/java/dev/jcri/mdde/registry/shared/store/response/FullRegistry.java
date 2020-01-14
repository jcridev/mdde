package dev.jcri.mdde.registry.shared.store.response;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.beans.Transient;
import java.util.*;

public class FullRegistry {
    private static final String REGISTRY_STATE_FIELD = "registry";
    /**
     * Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
     */
    private Map<String, Map<String, Set<String>>> _registry;

    /**
     * Construct registry
     * @param registry Map<Node ID, Map<Fragment ID, List<Tuple ID>>>
     */
    public FullRegistry(Map<String, Map<String, Set<String>>> registry){
        Objects.requireNonNull(registry, "Registry map can't be null");
        _registry = registry;
    }

    /**
     * Default constructor
     */
    public FullRegistry(){ _registry = null;};

    @JsonGetter(REGISTRY_STATE_FIELD)
    public Map<String, Map<String, Set<String>>> getRegistry() {
        return _registry;
    }
    @JsonSetter(REGISTRY_STATE_FIELD)
    public void setRegistry(Map<String, Map<String, Set<String>>> registry) {
        _registry = registry;
    }

    @Transient
    @JsonIgnore
    public Set<String> getNodes(){
        return _registry.keySet();
    }

    @Transient
    @JsonIgnore
    public Map<String, Set<String>> getNodeContents(String nodeId){
        if(nodeId == null || nodeId.isEmpty()){
            throw new IllegalArgumentException("Node ID must be supplied");
        }

        return  _registry.get(nodeId);
    }

    @Transient
    @JsonIgnore
    public Set<String> getNodeFragments(String nodeId){
        Map<String, Set<String>> contents = getNodeContents(nodeId);
        if(contents == null){
            return new HashSet<String>(); // return empty value
        }
        return contents.keySet();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FullRegistry)){
            return false;
        }
        FullRegistry objB = (FullRegistry) obj;
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
            Boolean equals = node.getValue().entrySet().stream()
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
