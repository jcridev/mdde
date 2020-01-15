package dev.jcri.mdde.registry.benchmark;

import dev.jcri.mdde.registry.benchmark.cluster.IReadOnlyTupleLocator;
import dev.jcri.mdde.registry.benchmark.cluster.ITupleLocatorFactory;
import dev.jcri.mdde.registry.benchmark.ycsb.EWorkloadCatalog;
import dev.jcri.mdde.registry.benchmark.ycsb.EYCSBClients;
import dev.jcri.mdde.registry.benchmark.ycsb.YCSBRunner;
import dev.jcri.mdde.registry.benchmark.ycsb.cli.YCSBOutput;
import dev.jcri.mdde.registry.configuration.RegistryConfig;
import dev.jcri.mdde.registry.configuration.benchmark.YCSBConfig;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.benchmark.commands.LocateTuple;
import dev.jcri.mdde.registry.shared.benchmark.responses.TupleLocation;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import dev.jcri.mdde.registry.shared.configuration.MDDERegistryNetworkConfiguration;
import dev.jcri.mdde.registry.shared.store.response.TupleCatalog;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class BenchmarkRunner {
    private final ITupleLocatorFactory _tupleLocatorFactory;
    private final IReadCommandHandler _storeReader;

    public BenchmarkRunner(ITupleLocatorFactory tupleLocatorFactory,
                           IReadCommandHandler storeReader){
        Objects.requireNonNull(tupleLocatorFactory);
        Objects.requireNonNull(storeReader);
        _tupleLocatorFactory = tupleLocatorFactory;
        _storeReader = storeReader;
    }

    private IReadOnlyTupleLocator _tmpTupleLocator = null;

    /**
     * Make preparations for running a benchmark.
     * @throws MddeRegistryException
     */
    public synchronized void prepareBenchmarkEnvironment()
            throws MddeRegistryException {
        IReadOnlyTupleLocator newLocator = _tupleLocatorFactory.getNewTupleLocator();
        TupleCatalog currentTupleStoreSnapshot = _storeReader.getTupleCatalog();
        newLocator.initializeDataLocator(currentTupleStoreSnapshot);
        _tmpTupleLocator = newLocator;
    }

    /**
     * Call after running a benchmark. It disposes of all resources and memory objects that are needed only at a time
     * of executing the benchmark.
     * @throws IOException
     */
    public synchronized void disposeBenchmarkEnvironment()
            throws IOException {
        try {
            if (_tmpTupleLocator != null) _tmpTupleLocator.close();
        } finally {
            _tmpTupleLocator = null;
        }
    }

    private void verifyState(){
        if(_tmpTupleLocator == null){
            throw new IllegalStateException("Benchmark runner has no initialized benchmark environment. " +
                    "You must call prepareBenchmarkEnvironment() prior executing the benchmark.");
        }
    }


    /**
     * Execute benchmark in the prepared environment.
     * @return
     */
    public YCSBOutput executeYCSBBenchmark(YCSBConfig ycsbConfig,
                                           String tempFolder,
                                           List<DBNetworkNodesConfiguration> dataNodes,
                                           MDDERegistryNetworkConfiguration registryNetworkInterfaces,
                                           EWorkloadCatalog workload,
                                           EYCSBClients client)
            throws IOException {
        verifyState();
        var ycsbRunner = new YCSBRunner(ycsbConfig, tempFolder, dataNodes, registryNetworkInterfaces);

        return null;
    }

    public TupleLocation getTupleLocation(LocateTuple tupleParams){
        verifyState();
        var result = _tmpTupleLocator.getNodeForRead(tupleParams.getTupleId());
        return new TupleLocation(result);
    }
}
