package dev.jcri.mdde.registry.configuration.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jcri.mdde.registry.configuration.RedisNodeConfig;
import dev.jcri.mdde.registry.configuration.RegistryConfig;
import dev.jcri.mdde.registry.configuration.RegistryDataStoreConfig;


/**
 * Read configuration YAML. Redis registry and Redis data store
 */
public class ConfigReaderYamlAllRedis implements IConfigReader<RegistryDataStoreConfig, RedisNodeConfig> {

    @Override
    public RegistryConfig readConfig(String fromString) throws Exception {
        var mapper = new ObjectMapper(new YAMLFactory());
        RegistryConfig<RegistryDataStoreConfig, RedisNodeConfig> result = mapper.readValue(fromString,  new TypeReference<RegistryConfig<RegistryDataStoreConfig, RedisNodeConfig>>() {});
        return result;
    }
}
