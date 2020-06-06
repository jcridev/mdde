package dev.jcri.mdde.registry.benchmark.counterfeit;

import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkFragmentStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkNodeStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.utility.MapTools;

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

    public BenchmarkRunResult estimateBenchmarkRun(double adjustRangeStart, double adjustRangeEnd){
        if(this._currentSettings == null){
            throw new IllegalStateException("Counterfeit benchmark runner was not initialized with the benchmark " +
                    "parameters.");
        }

        if(adjustRangeStart > adjustRangeEnd || adjustRangeEnd < 0 || adjustRangeStart < 0){
            throw new IllegalArgumentException("Adjustment range start must be > 0, " +
                    "adjustment range end must be > adjustment range end.");
        }

        // TODO: Refactor (inefficient runner)
        var fragmentCatalog = _storeReader.getFragmentCatalog(null, null);
        // Retrieve exact node contents
        Map<String, List<String>> nodeContents = new HashMap<>();

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

        // Pre-calculate fragment read distribution
        Map<String, int[]> fragmentReadDistribution = new HashMap<>();

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

            fragmentReadDistribution.put(fragReplicaId, reads);
        }

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
        // Throughput estimation
        // We make a naive assumption that if the reads are spread equally across the nodes, the better is throughput.
        var readSums = new int[nodeContents.size()];
        double[] readNodeParticipationBaseline = new double[readSums.length];
        for(var nodeId: nodeContents.keySet()) {
            // Current reads
            var nodeIdx = nodeIndexes.get(nodeId);
            for(var nodeStat: nodeStats.stream().filter(i -> i.getNodeId().equals(nodeId)).collect(Collectors.toList())){
                readSums[nodeIdx] = readSums[nodeIdx] + nodeStat.getTotalNumberOfReads();
            }
            // Baseline participation values
            readNodeParticipationBaseline[nodeIdx] = this._currentSettings.getNodeReadBalance().get(nodeId);
        }

        double totalReads = (double)IntStream.of(readSums).sum();
        double[] currentParticipation = new double[readSums.length];
        for(int i = 0; i < readSums.length; i++){
            currentParticipation[i] = readSums[i]/totalReads;
        }

        var baselineDisbalance = getParticipationDisbalance(readNodeParticipationBaseline);
        var currentDisbalance = getParticipationDisbalance(currentParticipation);
        int changeDirection = baselineDisbalance >= currentDisbalance ? 1 : -1;

        double baselineThroughput = this._currentSettings.getBaselineThroughput();
        double maxDisbalance = getParticipationTotalDisbalance(currentParticipation.length);

        double valueDiminisher = Math.abs(baselineDisbalance - currentDisbalance)
                * ((adjustRangeEnd - adjustRangeStart) / maxDisbalance) + adjustRangeStart;

        // Estimate throughput
        double estimatedThroughput = baselineThroughput
                + (baselineThroughput
                    * (Math.abs(baselineDisbalance - currentDisbalance) / maxDisbalance)
                        *  valueDiminisher
                    * changeDirection);

        // Get theoretical highest
        double estimatedBestThroughput = baselineThroughput
                + (baselineThroughput
                    * (Math.abs(baselineDisbalance - 0) / maxDisbalance)
                        * valueDiminisher
                    * 1);
        // Get theoretical lowest
        double estimatedWorstThroughput = baselineThroughput
                + (baselineThroughput
                    * (Math.abs(baselineDisbalance - maxDisbalance) / maxDisbalance)
                        * valueDiminisher
                    * baselineDisbalance >= maxDisbalance ? 1 : -1);

        // Fill out the result
        var result = new BenchmarkRunResult();

        var infoDict = result.getInfo();
        if (infoDict == null){
            infoDict = new HashMap<>();
            result.setInfo(infoDict);
        }
        // Add maximum throughput estimation
        infoDict.put("max_e_t", String.valueOf(estimatedBestThroughput));
        // Add worst throughput estimation
        infoDict.put("min_e_t", String.valueOf(estimatedWorstThroughput));

        result.setThroughput(estimatedThroughput);
        result.setNodes(nodeStats);
        return result;
    }

    /**
     * Calculate the participation disbalance value across the nodes
     * @param partP Participation percentages for all nodes.
     * @return 0 - perfect balance, the higher the  value, the higher is total disbalance.
     */
    private double getParticipationDisbalance(double[] partP){
        double disbalance = 0;
        for(double x: partP){
            for(double y: partP){
                disbalance += Math.abs(x - y);
            }
        }
        return disbalance;
    }

    /**
     * Get the edge-case value of total disbalance (all reads are from the same node). This is going to be the highest
     * degree of disbalance possible in this setup.
     * @param numOfNodes Total number of nodes.
     * @return Maximum disbalance value.
     */
    private double getParticipationTotalDisbalance(int numOfNodes){
        if (numOfNodes < 1){
            throw new IllegalArgumentException("numOfNodes must be > 0");
        }

        return (numOfNodes - 1) * 2;
    }
}
