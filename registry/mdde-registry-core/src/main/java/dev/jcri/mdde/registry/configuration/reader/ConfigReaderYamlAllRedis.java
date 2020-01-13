package dev.jcri.mdde.registry.configuration.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jcri.mdde.registry.configuration.RegistryConfig;
import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;


/**
 * Read configuration YAML. Redis registry
 */
public class ConfigReaderYamlAllRedis implements IConfigReader<RegistryStoreConfigRedis> {

    @Override
    public RegistryConfig<RegistryStoreConfigRedis> readConfig(String fromString) throws Exception {
        var mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(fromString,  new TypeReference<RegistryConfig<RegistryStoreConfigRedis>>() {});
    }
}
