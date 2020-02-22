package dev.jcri.mdde.registry.store.queue.impl.redis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;

import java.util.Set;
import java.util.UUID;

public class DataCopyActionJson extends DataCopyAction {
    @Override
    public void setActionId(UUID actionId){
        super.setActionId(actionId);
    }
    @Override
    public void setTupleIds(Set<String> tupleIds){
        super.setTupleIds(tupleIds);
    }
    @Override
    public void setSourceNode(String node){
        super.setSourceNode(node);
    }
    @Override
    public void setDestinationNode(String node){
        super.setDestinationNode(node);
    }
}
