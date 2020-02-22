package dev.jcri.mdde.registry.configuration.reader;

import dev.jcri.mdde.registry.configuration.RegistryConfig;


public interface IConfigReader<TStore> {
    public RegistryConfig<TStore> readConfig(String fromString) throws Exception;
}
