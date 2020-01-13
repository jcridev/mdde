package dev.jcri.mdde.registry.configuration.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jcri.mdde.registry.configuration.RegistryConfig;
import dev.jcri.mdde.registry.configuration.benchmark.YCSBConfig;
import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfigReaderYaml {

    @Test
    public void testSerializationDeserialization(){
        RegistryConfig<RegistryStoreConfigRedis> redisBackedConfig = new RegistryConfig<>();
        redisBackedConfig.setRegistryStore(new RegistryStoreConfigRedis());
        redisBackedConfig.getRegistryStore().setPort(1234);
        redisBackedConfig.getRegistryStore().setHost("localtest");

        DBNetworkNodesConfiguration redisNode1 = new DBNetworkNodesConfiguration();
        DBNetworkNodesConfiguration redisNode2 = new DBNetworkNodesConfiguration();
        redisNode1.setNodeId("R-node-1");
        redisNode1.setHost("192.168.0.101");
        redisNode1.setPort(1234);
        redisNode2.setNodeId("R-node-2");
        redisNode2.setHost("192.168.0.102");
        redisNode1.setPort(4321);
        redisBackedConfig.setDataNodes(new LinkedList<>(){{add(redisNode1);} {add(redisNode2);}});

        YCSBConfig testYCSBConfig = new YCSBConfig();
        testYCSBConfig.setYcsbBin("/test/folder/somewhere/bin");
        testYCSBConfig.setYcsbClient("Client.MDDE.Client");
        redisBackedConfig.setBenchmarkYcsb(testYCSBConfig);

        var mapper = new ObjectMapper(new YAMLFactory());
        String stringYaml = null;
        try {
            stringYaml = mapper.writeValueAsString(redisBackedConfig);
        } catch (JsonProcessingException e) {
            fail(e);
;       }
        assertNotNull(stringYaml);
        System.out.println(stringYaml);

        ConfigReaderYamlAllRedis configReader = new ConfigReaderYamlAllRedis();
        RegistryConfig<RegistryStoreConfigRedis> deserialized = null;
        try {
            deserialized = configReader.readConfig(stringYaml);
        } catch (Exception e) {
            fail(e);
        }
        assertNotNull(deserialized);

        assertEquals(redisBackedConfig.getRegistryStore().getHost(), deserialized.getRegistryStore().getHost());
        assertEquals(redisBackedConfig.getDataNodes().get(0).getNodeId(), deserialized.getDataNodes().get(0).getNodeId());
        assertEquals(redisBackedConfig.getDataNodes().get(1).getNodeId(), deserialized.getDataNodes().get(1).getNodeId());
    }

    @Test
    public void testOutOfOrderDeserialization() {
        var sample = "---\n" +
                "nodes:\n" +
                "- id: \"R-node-1\"\n" +
                "  host: \"192.168.0.101\"\n" +
                "  port: 4321\n" +
                "  username: null\n" +
                "  password: null\n" +
                "  dir: null\n" +
                "- id: \"R-node-2\"\n" +
                "  username: null\n" +
                "  password: null\n" +
                "  host: \"192.168.0.102\"\n" +
                "  port: 6379  \n" +
                "  dir: null\n" +
                "store:\n" +
                "  redis_port: 1234\n" +
                "  redis_host: \"localtest\"  \n" +
                "  redis_password: null\n" +
                "bench_ycsb:  \n" +
                "  client: \"Client.MDDE.Client\"\n" +
                "  bin: \"/test/folder/somewhere/bin\"";

        ConfigReaderYamlAllRedis configReader = new ConfigReaderYamlAllRedis();
        RegistryConfig<RegistryStoreConfigRedis> deserialized = null;
        try {
            deserialized = configReader.readConfig(sample);
        } catch (Exception e) {
            fail(e);
        }
        assertNotNull(deserialized);

        assertEquals("localtest", deserialized.getRegistryStore().getHost());
        assertEquals("R-node-1", deserialized.getDataNodes().get(0).getNodeId());
        assertEquals("192.168.0.102", deserialized.getDataNodes().get(1).getHost());
    }
}
