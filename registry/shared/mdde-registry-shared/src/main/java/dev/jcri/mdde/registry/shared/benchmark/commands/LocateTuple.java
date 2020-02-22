package dev.jcri.mdde.registry.shared.benchmark.commands;

/**
 * Locate tuple benchmark operation parameter
 */
public class LocateTuple {
    /**
     * Tuple ID that should be located
     */
    protected String tupleId;

    /**
     * Default constructor
     */
    public LocateTuple(){}

    /**
     * Constructor
     * @param tupleId Tuple ID
     */
    public LocateTuple(String tupleId) {
        this.tupleId = tupleId;
    }

    public String getTupleId(){
        return tupleId;
    }
}
