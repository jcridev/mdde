package dev.jcri.mdde.registry.store.queue.actions;

import java.util.Set;

/**
 * Copy action
 */
public class DataCopyAction extends DataAction {
    private Set<String> _tupleIds;
    private String _sourceNode;
    private String _destinationNode;

    /**
     * Constructor
     * @param tupleIds Set of tuple IDs located on the source node that need to be copied to the destination node
     * @param sourceNode Registry ID of the source
     * @param destinationNode Registry ID of the destination node
     */
    public DataCopyAction(Set<String> tupleIds, String sourceNode, String destinationNode) {
        super(EActionType.COPY);

        setTupleIds(tupleIds);
        setSourceNode(sourceNode);
        setDestinationNode(destinationNode);
    }

    /**
     * Default protected constructor
     */
    protected DataCopyAction(){
        super(EActionType.COPY);
    }

    /**
     *  Set of tuple IDs located on the source node that need to be copied to the destination node
     * @return
     */
    public Set<String> getTupleIds() {
        return _tupleIds;
    }

    protected void setTupleIds(Set<String> tupleIds){
        if(tupleIds == null || tupleIds.isEmpty()){
            throw new IllegalArgumentException("Set of the tuple IDs can't be empty to perform a COPY operation");
        }
        _tupleIds = tupleIds;
    }

    /**
     * Registry ID of the source
     * @return
     */
    public String getSourceNode() {
        return _sourceNode;
    }

    protected void setSourceNode(String node){
        if(node == null || node.isBlank()){
            throw new IllegalArgumentException("Source data node ID must be set to perform a COPY operation");
        }
        this._sourceNode = node;
    }

    /**
     * Registry ID of the destination node
     * @return
     */
    public String getDestinationNode() {
        return _destinationNode;
    }

    protected void setDestinationNode(String node){
        if(node == null || node.isBlank()){
            throw new IllegalArgumentException("Destination data node ID must be set to perform a COPY operation");
        }

        this._destinationNode = node;
    }
}
