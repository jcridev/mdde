package dev.jcri.mdde.registry.store.queue.impl.redis;

import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;

import java.util.Set;
import java.util.UUID;

public class DataDeleteActionJson extends DataDeleteAction {
    @Override
    public void setActionId(UUID actionId){
        super.setActionId(actionId);
    }
    @Override
    public void setTupleIds(Set<String> tupleIds){
        super.setTupleIds(tupleIds);
    }
    @Override
    public void setDataNode(String node){
        super.setDataNode(node);
    }
}
