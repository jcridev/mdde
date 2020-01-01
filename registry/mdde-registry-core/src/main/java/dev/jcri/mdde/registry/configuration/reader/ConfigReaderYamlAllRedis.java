package dev.jcri.mdde.registry.configuration.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jcri.mdde.registry.configuration.redis.DataNodeConfigRedis;
import dev.jcri.mdde.registry.configuration.RegistryConfig;
import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;


/**
 * Read configuration YAML. Redis registry and Redis data store
 */
public class ConfigReaderYamlAllRedis implements IConfigReader<RegistryStoreConfigRedis, DataNodeConfigRedis> {

    @Override
    public RegistryConfig<RegistryStoreConfigRedis, DataNodeConfigRedis> readConfig(String fromString) throws Exception {
        var mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(fromString,  new TypeReference<RegistryConfig<RegistryStoreConfigRedis, DataNodeConfigRedis>>() {});
    }
}
