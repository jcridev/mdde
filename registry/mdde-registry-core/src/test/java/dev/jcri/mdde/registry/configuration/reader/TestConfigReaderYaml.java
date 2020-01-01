package dev.jcri.mdde.registry.configuration.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jcri.mdde.registry.configuration.RedisNodeConfig;
import dev.jcri.mdde.registry.configuration.RegistryConfig;
import dev.jcri.mdde.registry.configuration.RegistryDataStoreConfig;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfigReaderYaml {

    @Test
    public void testSerializationDeserialization(){
        RegistryConfig<RegistryDataStoreConfig, RedisNodeConfig> redisBackedConfig = new RegistryConfig<>();
        redisBackedConfig.setRegistryStore(new RegistryDataStoreConfig());
        redisBackedConfig.getRegistryStore().setRedisPort(1234);
        redisBackedConfig.getRegistryStore().setRedisHost("localtest");

        RedisNodeConfig redisNode1 = new RedisNodeConfig();
        RedisNodeConfig redisNode2 = new RedisNodeConfig();
        redisNode1.setNodeId("R-node-1");
        redisNode2.setNodeId("R-node-2");
        redisBackedConfig.setDataNodes(new LinkedList<>(){{add(redisNode1);} {add(redisNode2);}});

        var mapper = new ObjectMapper(new YAMLFactory());
        String stringYaml = null;
        try {
            stringYaml = mapper.writeValueAsString(redisBackedConfig);
        } catch (JsonProcessingException e) {
            fail(e);
;       }
        assertNotNull(stringYaml);

        ConfigReaderYamlAllRedis configReader = new ConfigReaderYamlAllRedis();
        RegistryConfig<RegistryDataStoreConfig, RedisNodeConfig> deserialized = null;
        try {
            deserialized = configReader.readConfig(stringYaml);
        } catch (Exception e) {
            fail(e);
        }
        assertNotNull(deserialized);

        assertEquals(redisBackedConfig.getRegistryStore().getRedisHost(), deserialized.getRegistryStore().getRedisHost());
        assertEquals(redisBackedConfig.getDataNodes().get(0).getNodeId(), deserialized.getDataNodes().get(0).getNodeId());
        assertEquals(redisBackedConfig.getDataNodes().get(1).getNodeId(), deserialized.getDataNodes().get(1).getNodeId());
    }
}
