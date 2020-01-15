package dev.jcri.mdde.registry.shared.benchmark;

import dev.jcri.mdde.registry.shared.benchmark.commands.LocateTuple;
import dev.jcri.mdde.registry.shared.benchmark.responses.TupleLocation;

import java.io.Closeable;
import java.io.IOException;

/**
 * Benchmark client for MDDE-Registry. Meant to perform quick and simple operations during the data distribution
 * quality measurement process.
 */
public interface IMDDEBenchmarkClient extends Closeable {
    /**
     * Open connection to the server
     */
    void openConnection() throws InterruptedException, IOException;

    /**
     * Locate tuple
     * @param tupleParam Message container
     * @return Response
     */
    TupleLocation locateTuple(LocateTuple tupleParam) throws InterruptedException;
}
