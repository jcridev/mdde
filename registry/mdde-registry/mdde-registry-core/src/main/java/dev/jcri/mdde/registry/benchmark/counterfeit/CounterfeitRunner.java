package dev.jcri.mdde.registry.benchmark.counterfeit;

import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkFragmentStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkNodeStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.utility.MapTools;
import org.apache.kafka.common.protocol.types.Field;

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

        List<FragmentStats> listOfFragments = new ArrayList<>();

        for (var fragReplicaId: fragmentCatalog.getFragments().keySet()) {
            var expectedReadsTotal = this._currentSettings.getReads().get(fragReplicaId);

            var cFragmentStats = new FragmentStats(fragReplicaId);
            cFragmentStats.totalExpectedReads = expectedReadsTotal;

            List<Integer> fragNodeIdx = IntStream.range(0, nodeContentsToIndexes.size())
                    .filter(i -> nodeContentsToIndexes.get(i).contains(fragReplicaId))
                    .collect(ArrayList<Integer>::new, ArrayList::add, ArrayList::addAll);

            cFragmentStats.setAllocationNodeIdx(fragNodeIdx);

            listOfFragments.add(cFragmentStats);
        }
        // Sort by Number of replicas (ascending) then by Number of reads (descending)
        Collections.sort(listOfFragments);

        int[] totalNodeReadCounter = new int[nodeIndexes.size()];
        for (var fragReplicaInfo: listOfFragments){
            var fragmentExpectedReadsTotal = fragReplicaInfo.totalExpectedReads;
            var fragmentReplicaCounter = fragReplicaInfo.getNumberOfReplicas();
            var fragmentReplicasAllocation = fragReplicaInfo.getAllocationNodeIdx();

            int[] reads = new int[nodeIndexes.size()];
            // *Naive*, spread read equally among the nodes that have the fragments.
            // To be more realistic, would need to take a temporal aspect of the benchmark reads as well into account,
            // but that's for the future versions.
            if (fragmentReplicaCounter == 1){
                // A single replica (no choice where to put it)
                var nodeIdx = fragmentReplicasAllocation.get(0);
                totalNodeReadCounter[nodeIdx] = totalNodeReadCounter[nodeIdx] + fragmentExpectedReadsTotal;
                reads[nodeIdx] = fragmentExpectedReadsTotal;
            }
            else if (fragmentExpectedReadsTotal <= fragmentReplicaCounter){
                // Multiple replicas but less reads than replicas, or equal number of reads and replicas.
                // Pointless to implement any complex calculations for such a small number.
                for(int i = 0; i < fragmentExpectedReadsTotal; i++){
                    var nodeIdx = fragmentReplicasAllocation.get(i);
                    totalNodeReadCounter[nodeIdx] = totalNodeReadCounter[nodeIdx] + 1;
                    reads[nodeIdx] = 1;
                }
            }
            else{
                // Multiple replicas, more reads than replicas.
                // TODO: Refactor
                // Get total current number of reads for nodes where fragment replicas are allocated
                int currentTotalReadsForFragAllocNodes = 0;
                for(var allocationNodeIdx: fragmentReplicasAllocation){
                    currentTotalReadsForFragAllocNodes += totalNodeReadCounter[allocationNodeIdx];
                }
                // Calculate the current degree of participation per selected node
                List<SelectedNodeRead> currentAllocatedNodeParticipation = new ArrayList<>();
                for(var allocationNodeIdx: fragmentReplicasAllocation){
                    currentAllocatedNodeParticipation.add(
                            new SelectedNodeRead(
                                allocationNodeIdx,
                                    currentTotalReadsForFragAllocNodes > 0 ?
                                            (double)totalNodeReadCounter[allocationNodeIdx] / currentTotalReadsForFragAllocNodes
                                            : 1.0 / fragmentReplicaCounter
                            ));
                }

                Collections.sort(currentAllocatedNodeParticipation);
                var reversed = new ArrayList<>(currentAllocatedNodeParticipation);
                Collections.reverse(reversed);
                // Distribute reads among the nodes taking into account their current participation degree
                int remainingReads = fragmentExpectedReadsTotal;
                int processedNodes = 0;
                int lastNode = currentAllocatedNodeParticipation.size() - 1;
                for (int i = 0; i < currentAllocatedNodeParticipation.size(); i++) {
                    SelectedNodeRead nodeParticipation = currentAllocatedNodeParticipation.get(i);
                    SelectedNodeRead oppositeNodeParticipation = reversed.get(i);
                    if (processedNodes < lastNode) {
                        int chunk = (int) Math.ceil(fragmentExpectedReadsTotal * oppositeNodeParticipation.getParticipation());
                        totalNodeReadCounter[nodeParticipation.getNodeIdx()] = totalNodeReadCounter[nodeParticipation.getNodeIdx()] + chunk;
                        reads[nodeParticipation.getNodeIdx()] = chunk;
                        remainingReads = remainingReads - chunk;
                    } else {
                        totalNodeReadCounter[nodeParticipation.getNodeIdx()] = totalNodeReadCounter[nodeParticipation.getNodeIdx()] + remainingReads;
                        reads[nodeParticipation.getNodeIdx()] = remainingReads;
                    }
                    processedNodes++;
                }
            }

            fragmentReadDistribution.put(fragReplicaInfo.getFragmentId(), reads);
        }

        // Attempt to redistribute reads

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
        int changeDirection = baselineDisbalance > currentDisbalance ? -1 : 1;

        double baselineThroughput = this._currentSettings.getBaselineThroughput();
        double maxDisbalance = getParticipationTotalDisbalance(currentParticipation.length);

        double degreeOfChange = (Math.abs(baselineDisbalance - currentDisbalance) / maxDisbalance);

        double valueDiminisher = Math.abs(baselineDisbalance - currentDisbalance)
                * ((adjustRangeEnd - adjustRangeStart) / maxDisbalance) + adjustRangeStart;

        // Estimate throughput

        double estimatedThroughput = Math.pow(10, Math.log10(baselineThroughput)
                -(changeDirection) * Math.log10(1 + degreeOfChange + valueDiminisher));

        // Get theoretical highest
        double estimatedBestThroughput = Math.pow(10, Math.log10(baselineThroughput)
                -(changeDirection) * Math.log10(1 + (baselineDisbalance / maxDisbalance) + valueDiminisher));

        // Get theoretical lowest
        double estimatedWorstThroughput = Math.pow(10, Math.log10(baselineThroughput)
                -(baselineDisbalance >= maxDisbalance ? 1 : -1)
                    * Math.log10(1 + (Math.abs(baselineDisbalance - maxDisbalance) / maxDisbalance) + valueDiminisher));

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

    private class FragmentStats implements Comparable<FragmentStats>{
        private List<Integer> _allocationNodeIdx;
        private int _numberOfReplicas = 0;

        private final String _fragmentId;
        public int totalExpectedReads = 0;

        public FragmentStats(String fragmentId){
            Objects.requireNonNull(fragmentId);
            _fragmentId = fragmentId;
        }

        public String getFragmentId(){
            return _fragmentId;
        }

        public int getNumberOfReplicas(){
            return _numberOfReplicas;
        }

        public List<Integer> getAllocationNodeIdx() {
            return _allocationNodeIdx;
        }

        public void setAllocationNodeIdx(List<Integer> value) {
            this._allocationNodeIdx = value;

            if(this._allocationNodeIdx != null){
                _numberOfReplicas = this._allocationNodeIdx.size();
            }
            else{
                _numberOfReplicas = 0;
            }
        }

        @Override
        public int compareTo(CounterfeitRunner.FragmentStats o2) {
            var o1 = this;
            // Ascending replicas order
            int numOfReplicasRes = Integer.compare(o1.getNumberOfReplicas(), o2.getNumberOfReplicas());
            if (numOfReplicasRes != 0)
            {
                return numOfReplicasRes;
            }
            // Descending reads order
            return Integer.compare(o2.totalExpectedReads, o1.totalExpectedReads);
        }
    }

    private class SelectedNodeRead implements Comparable<SelectedNodeRead>{
        private int _nodeIdx = -1;
        private double _participation = -1.0;

        public SelectedNodeRead(int nodeIdx, double participation){
            _nodeIdx = nodeIdx;
            _participation = participation;
        }

        public int getNodeIdx(){
            return _nodeIdx;
        }

        public double getParticipation(){
            return _participation;
        }

        @Override
        public int compareTo(CounterfeitRunner.SelectedNodeRead o2) {
            var o1 = this;
            return Double.compare(o2.getParticipation(), o1.getParticipation());
        }
    }
}
