package dev.jcri.mdde.registry.store.queue.actions;

import java.util.Set;

/**
 * Delete tuples from the specific node data action
 */
public class DataDeleteAction extends DataAction {
    private Set<String> _tupleIds;
    private String _dataNode;

    /**
     * Constructor
     * @param tupleIds Set of tuple IDs that must be removed from the specified data node
     * @param dataNode Data node registry ID
     */
    public DataDeleteAction(Set<String> tupleIds, String dataNode) {
        super(EActionType.DELETE);

        setTupleIds(tupleIds);
        setDataNode(dataNode);
    }

    protected DataDeleteAction(){
        super(EActionType.DELETE);
    }

    public Set<String> getTupleIds() {
        return _tupleIds;
    }

    protected void setTupleIds(Set<String> tupleIds){
        if(tupleIds == null || tupleIds.isEmpty()){
            throw new IllegalArgumentException("Set of the tuple IDs can't be empty to perform a COPY operation");
        }
        _tupleIds = tupleIds;
    }

    public String getDataNode() {
        return _dataNode;
    }

    protected void setDataNode(String node){
        if(node == null || node.isBlank()){
            throw new IllegalArgumentException("Source data node ID must be set to perform a COPY operation");
        }
        this._dataNode = node;
    }
}
