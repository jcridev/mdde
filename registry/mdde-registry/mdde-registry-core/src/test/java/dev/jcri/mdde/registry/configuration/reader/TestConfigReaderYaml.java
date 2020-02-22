package dev.jcri.mdde.registry.configuration.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jcri.mdde.registry.configuration.RegistryConfig;
import dev.jcri.mdde.registry.configuration.benchmark.YCSBConfig;
import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.MDDEClientConfiguration;
import dev.jcri.mdde.registry.shared.benchmark.ycsb.MDDEClientConfigurationWriter;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfigReaderYaml {

    private RegistryConfig<RegistryStoreConfigRedis> generateRedisConfig(){
        RegistryConfig<RegistryStoreConfigRedis> redisBackedConfig = new RegistryConfig<>();
        // Registry storage
        redisBackedConfig.setRegistryStore(new RegistryStoreConfigRedis());
        redisBackedConfig.getRegistryStore().setPort(16379);
        redisBackedConfig.getRegistryStore().setHost("localhost");
        // Data nodes
        DBNetworkNodesConfiguration redisNode0 = new DBNetworkNodesConfiguration();
        DBNetworkNodesConfiguration redisNode1 = new DBNetworkNodesConfiguration();
        DBNetworkNodesConfiguration redisNode2 = new DBNetworkNodesConfiguration();
        DBNetworkNodesConfiguration redisNode3 = new DBNetworkNodesConfiguration();
        redisNode0.setNodeId("redis_node_0");
        redisNode0.setHost("localhost");
        redisNode0.setPort(16480);

        redisNode1.setNodeId("redis_node_1");
        redisNode1.setHost("localhost");
        redisNode1.setPort(16481);

        redisNode2.setNodeId("redis_node_2");
        redisNode2.setHost("localhost");
        redisNode2.setPort(16482);

        redisNode3.setNodeId("redis_node_3");
        redisNode3.setHost("localhost");
        redisNode3.setPort(16483);

        redisBackedConfig.setDataNodes(new LinkedList<>()
        {
            {add(redisNode0);}
            {add(redisNode1);}
            {add(redisNode2);}
            {add(redisNode3);}
        });

        return redisBackedConfig;
    }

    @Test
    public void testSerializationDeserialization(){
        var redisBackedConfig = generateRedisConfig();
        // YCSB config
        YCSBConfig testYCSBConfig = new YCSBConfig();
        testYCSBConfig.setYcsbBin("/test/folder/somewhere/bin");
        testYCSBConfig.setYcsbClient("mdde.redis");
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
                "store:\n" +
                "  redis_host: \"localhost\"\n" +
                "  redis_port: 16379\n" +
                "  redis_password: null\n" +
                "nodes:\n" +
                "- id: \"redis_node_0\"\n" +
                "  host: \"localhost\"\n" +
                "  port: 16480\n" +
                "  username: null\n" +
                "  password: null\n" +
                "  dir: null\n" +
                "  default: true\n" +
                "- id: \"redis_node_1\"\n" +
                "  host: \"localhost\"\n" +
                "  port: 16481\n" +
                "  username: null\n" +
                "  password: null\n" +
                "  dir: null\n" +
                "  default: true\n" +
                "- id: \"redis_node_2\"\n" +
                "  host: \"localhost\"\n" +
                "  port: 16482\n" +
                "  username: null\n" +
                "  password: null\n" +
                "  dir: null\n" +
                "  default: true\n" +
                "- id: \"redis_node_3\"\n" +
                "  host: \"localhost\"\n" +
                "  port: 16483\n" +
                "  username: null\n" +
                "  password: null\n" +
                "  dir: null\n" +
                "  default: true\n" +
                "bench_ycsb:\n" +
                "  bin: \"/test/folder/somewhere/bin\"\n" +
                "  client: \"mdde.redis\"\n" +
                "temp: \".\\\\temp\"";

        ConfigReaderYamlAllRedis configReader = new ConfigReaderYamlAllRedis();
        RegistryConfig<RegistryStoreConfigRedis> deserialized = null;
        try {
            deserialized = configReader.readConfig(sample);
        } catch (Exception e) {
            fail(e);
        }
        assertNotNull(deserialized);

        assertEquals("localhost", deserialized.getRegistryStore().getHost());
        assertEquals("redis_node_0", deserialized.getDataNodes().get(0).getNodeId());
        assertEquals(16481, deserialized.getDataNodes().get(1).getPort());
    }

    @Test
    public void testCreateYCSBClientConfig(){
        var redisBackedConfig = generateRedisConfig();

        var registryNetworkInterfaces = new HashMap<String, String>();
        registryNetworkInterfaces.put("host","localhost");
        registryNetworkInterfaces.put("port",Integer.toString(8942));
        registryNetworkInterfaces.put("portBench",Integer.toString(8954));

        var newClientConfig = new MDDEClientConfiguration();
        newClientConfig.setNodes(redisBackedConfig.getDataNodes());
        newClientConfig.setRegistryNetworkConnection(registryNetworkInterfaces);

        var configWriter = new MDDEClientConfigurationWriter();
        String newClientConfigYML = null;
        try {
            newClientConfigYML = configWriter.serializeConfiguration(newClientConfig);
        } catch (JsonProcessingException e) {
            fail(e);
        }

        assertNotNull(newClientConfigYML);
        System.out.println(newClientConfigYML);
    }
}
