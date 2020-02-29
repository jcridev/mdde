package dev.jcri.mdde.registry.shared.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkFragmentStats;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkNodeStats;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestSerialization {

    @Test
    public void ycsbStats() throws JsonProcessingException {
        Collection<BenchmarkNodeStats> testNodes = new ArrayList<>();

        final int targetNumNodes = 4;
        final int targetNumFragsPerNode = 25;

        for(int i=0; i<targetNumNodes;i++ ){
            BenchmarkNodeStats node = new BenchmarkNodeStats();
            node.setNodeId(UUID.randomUUID().toString());

            List<BenchmarkFragmentStats> fragments = new ArrayList<>();
            for(int j=0; j<targetNumFragsPerNode;j++){
                BenchmarkFragmentStats fragment = new BenchmarkFragmentStats(UUID.randomUUID().toString(), j);
                fragments.add(fragment);
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

            for(BenchmarkFragmentStats fragment: node.getFragments()){
                BenchmarkFragmentStats testFragment = testNode.getFragments()
                        .stream()
                        .filter(n -> n.getFragmentId().equals(fragment.getFragmentId()))
                        .findFirst().orElse(null);

                assertNotNull(testFragment);
                assertEquals(testFragment.getFragmentId(), fragment.getFragmentId());
                assertEquals(testFragment.getReadCount(), fragment.getReadCount());
            }
        }
    }
}
