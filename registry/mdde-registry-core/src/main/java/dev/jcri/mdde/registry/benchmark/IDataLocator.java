package dev.jcri.mdde.registry.benchmark;

public interface IDataLocator {
    /**
     * Get Node Id which is most suitable for reading a tuple from. Even if tuple is replicated on multiple nodes, we
     * should return only one, which is mose suitable. This is point where the mock cluster logic is implemented.
     *
     * Implementation of the method is dependant on the chosen execution strategy.
     *
     * This should be returned to the benchmark runner (YCSB).
     * @param tupleId Tuple Id
     * @return Node Id
     */
    String getNodeForRead(String tupleId);


}
