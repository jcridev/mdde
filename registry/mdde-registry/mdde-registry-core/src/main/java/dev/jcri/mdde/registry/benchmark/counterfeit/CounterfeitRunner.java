package dev.jcri.mdde.registry.benchmark.counterfeit;

import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkFragmentStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkNodeStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.utility.MapTools;
import dev.jcri.mdde.registry.utility.MutableCounter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CounterfeitRunner {

    private final IReadCommandHandler _storeReader;
    private CounterfeitBenchSettings _currentSettings = null;

    public CounterfeitRunner(IReadCommandHandler storeReader){
        Objects.requireNonNull(storeReader);
        _storeReader = storeReader;
    }


    public CounterfeitBenchSettings getCurrentSettings() {
        return _currentSettings;
    }

    public void setCurrentSettings(CounterfeitBenchSettings currentSettings) {
        this._currentSettings = currentSettings;
    }

    /**
     * Check if counterfeit runner is ready.
     * @return True - counterfeit benchmark result can be received.
     */
    public boolean isReady(){
        return _currentSettings != null;
    }

    public BenchmarkRunResult estimateBenchmarkRun(){
        if(this._currentSettings == null){
            throw new IllegalStateException("Counterfeit benchmark runner was not initialized with the benchmark " +
                    "parameters.");
        }

        // TODO: Refactor (inefficient runner)
        var fragmentCatalog = _storeReader.getFragmentCatalog(null, null);
        // Retrieve exact node contents
        Map<String, List<String>> nodeContents = new HashMap<>();
        //Map<String, MutableCounter> fragmentReplicas = new HashMap<>();

        Map<Integer, String> catalogNodeId = MapTools.invert(fragmentCatalog.getNodes());
        Map<Integer, String> catalogFragmentId = MapTools.invert(fragmentCatalog.getFragments());

        for (var nodeCatId : fragmentCatalog.getNodeContent().keySet()){
            var nodeId = catalogNodeId.get(nodeCatId);
            var nodeFragsCatIds = fragmentCatalog.getNodeContent().get(nodeCatId);
            List<String> nodeFrags = new ArrayList<>();
            for (var frag : nodeFragsCatIds){
                // Fill out the map
                var fragId = catalogFragmentId.get(frag);
                nodeFrags.add(fragId);
                /*
                // Increment total fragment replicas counter
                var fragRCounter = fragmentReplicas.get(fragId);
                if (fragRCounter != null) {
                    fragRCounter.increment();
                } else {
                    fragmentReplicas.put(fragId, new MutableCounter(1));
                } */
            }
            nodeContents.put(nodeId, nodeFrags);
        }

        Map<String, Integer> nodeIndexes = new HashMap<>();
        ArrayList<List<String>> nodeContentsToIndexes = new ArrayList<>(nodeContents.size());
        var nodeCnt = 0;
        for(var nodeId: nodeContents.keySet()){
            nodeContentsToIndexes.add(nodeCnt, nodeContents.get(nodeId));
            nodeIndexes.put(nodeId, nodeCnt++);
        }
        int test = 0;
        // Pre-calculate fragment read distribution
        Map<String, int[]> fragmentReadDistribution = new HashMap<>();

        var fragsTest = fragmentCatalog.getFragments().keySet();

        for (var fragReplicaId: fragmentCatalog.getFragments().keySet()){
            var expectedReadsTotal = this._currentSettings.getReads().get(fragReplicaId);
            //var replicaCounter = fragmentReplicas.get(fragReplicaId);

            List<Integer> fragNodeIdx = IntStream.range(0, nodeContentsToIndexes.size())
                                        .filter(i -> nodeContentsToIndexes.get(i).contains(fragReplicaId))
                                        .collect(ArrayList<Integer>::new, ArrayList::add, ArrayList::addAll);
            var replicaCounter = fragNodeIdx.size();

            int[] reads = new int[nodeIndexes.size()];
            // *Naive*, spread read equally among the nodes that have the fragments.
            // To be more realistic, would need to take a temporal aspect of the benchmark reads as well into account,
            // but that's for the future versions.
            if (replicaCounter == 1){
                // A single replica
                reads[fragNodeIdx.get(0)] = expectedReadsTotal;
            }
            else if (expectedReadsTotal <= replicaCounter){
                // Multiple replicas but less reads than replicas, or equal number of reads and replicas.
                for(int i = 0; i < expectedReadsTotal; i++){
                    reads[fragNodeIdx.get(i)] = 1;
                }
            }
            else{
                // Multiple replicas, more reads than replicas
                int remainingReads = expectedReadsTotal;
                for(int i = 0; i < replicaCounter; i++){
                    int cDiv = replicaCounter - i;
                    int chunk = remainingReads / cDiv;
                    if (remainingReads % cDiv != 0){
                        chunk += 1;
                    }
                    reads[fragNodeIdx.get(i)] = chunk;
                    remainingReads = remainingReads - chunk;
                }
            }
            test += IntStream.of(reads).sum();
            fragmentReadDistribution.put(fragReplicaId, reads);
        }
        var t = this._currentSettings.getTotalReads();
        var s = test;
        // Calculate
        List<BenchmarkNodeStats> nodeStats = new ArrayList<>();
        for(var nodeId: nodeContents.keySet()){
            var nodeIdx = nodeIndexes.get(nodeId);
            Map<String, BenchmarkFragmentStats> fragmentStats = new HashMap<>();
            var resultNodeStats = new BenchmarkNodeStats("counterfeit", nodeId, fragmentStats);
            for(var nodeFragment: nodeContents.get(nodeId)){
                var nodeFragReads = fragmentReadDistribution.get(nodeFragment)[nodeIdx];
                var fragStats = new BenchmarkFragmentStats(nodeFragReads);
                fragmentStats.put(nodeFragment, fragStats);
            }
            nodeStats.add(resultNodeStats);
        }
        // Throughput
        // We make a naive assumption that if the reads are spread equally across the nodes, the better is throughput.
        var readSums = new int[nodeStats.size()];
        for (int i = 0; i < nodeStats.size(); i++) {
            BenchmarkNodeStats readDistNode = nodeStats.get(i);
            readSums[i] = readDistNode.getTotalNumberOfReads();
        }

        float totalReads = (float)IntStream.of(readSums).sum();
        float[] participationPercents = new float[readSums.length];
        for(int i = 0; i < readSums.length; i++){
            participationPercents[i] = readSums[i]/totalReads;
        }

        var result = new BenchmarkRunResult();
        result.setThroughput(this._currentSettings.getBaselineThroughput());
        result.setNodes(nodeStats);
        return result;
    }
}
