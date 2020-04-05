package dev.jcri.mdde.registry.shared.benchmark.ycsb;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Facilitates reading and deserialization of an MDDE client configuration file
 */
public class MDDEClientConfigurationReader extends MDDEClientConfigurationBase {

    /**
     * Deserialize MDDE client configuration from a string
     * @param config MDDE client configuration serialized as a string
     * @return MDDE client configuration object
     * @throws JsonProcessingException
     */
    public MDDEClientConfiguration deserializeConfiguration(String config) throws JsonProcessingException {
        return getMapper().readValue(config, MDDEClientConfiguration.class);
    }

    /**
     * Read MDDE Client configuration from a file
     * @param filePath path to the MDDE client configuration file
     * @return MDDE client configuration object
     * @throws IOException
     */
    public MDDEClientConfiguration readConfiguration(Path filePath) throws IOException {
        byte[] configBytes = Files.readAllBytes(filePath);
        String configString = new String(configBytes, configurationCharset);
        return deserializeConfiguration(configString);
    }
}
