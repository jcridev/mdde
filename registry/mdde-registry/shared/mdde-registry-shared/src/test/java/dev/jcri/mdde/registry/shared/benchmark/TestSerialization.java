package dev.jcri.mdde.registry.shared.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkFragmentStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkNodeStats;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TestSerialization {

    @Test
    public void ycsbStats() throws JsonProcessingException {
        List<BenchmarkNodeStats> testNodes = new ArrayList<>();

        final int targetNumNodes = 4;
        final int targetNumFragsPerNode = 25;

        for(int i=0; i<targetNumNodes;i++ ){
            BenchmarkNodeStats node = new BenchmarkNodeStats();
            node.setNodeId(UUID.randomUUID().toString());

            Map<String, BenchmarkFragmentStats> fragments = new HashMap<>();
            for(int j=0; j<targetNumFragsPerNode;j++){
                BenchmarkFragmentStats fragment = new BenchmarkFragmentStats(j);
                fragments.put(UUID.randomUUID().toString(), fragment);
            }
            node.setFragments(fragments);
            testNodes.add(node);
        }

        ObjectMapper mapper=new ObjectMapper();
        String json = mapper.writeValueAsString(testNodes);

        CollectionType pojoTypeReference =
                TypeFactory.defaultInstance().constructCollectionType(List.class, BenchmarkNodeStats.class);
        List<BenchmarkNodeStats> deserialized = mapper.readValue(json, pojoTypeReference);

        assertEquals(testNodes.size(), deserialized.size());
        for(BenchmarkNodeStats node: deserialized){
            BenchmarkNodeStats testNode = testNodes
                    .stream()
                    .filter(n -> n.getNodeId().equals(node.getNodeId()))
                    .findFirst().orElse(null);

            assertNotNull(testNode);
            assertEquals(testNode.getFragments().size(), node.getFragments().size());
            /**
            for(Map.Entry<String,BenchmarkFragmentStats> fragment: node.getFragments().entrySet()){
                BenchmarkFragmentStats testFragment = testNode.getFragments().entrySet()
                        .stream()
                        .filter(n -> n.getKey().equals(fragment.getKey()))
                        .findFirst().orElse(null);

                assertNotNull(testFragment);
                assertEquals(testFragment.getFragmentId(), fragment.getFragmentId());
                assertEquals(testFragment.getReadCount(), fragment.getReadCount());
            }  **/
        }
    }
}
