package dev.jcri.mdde.registry.configuration.reader;

import dev.jcri.mdde.registry.configuration.IDataNode;
import dev.jcri.mdde.registry.configuration.RegistryConfig;


public interface IConfigReader<Tstore, Tdata extends IDataNode> {
    public RegistryConfig<Tstore, Tdata> readConfig(String fromString) throws Exception;
}
