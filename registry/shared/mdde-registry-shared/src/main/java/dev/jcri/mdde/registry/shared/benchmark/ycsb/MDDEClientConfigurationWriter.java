package dev.jcri.mdde.registry.shared.benchmark.ycsb;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Facilitates writing and serialization of an MDDE configuration file
 */
public class MDDEClientConfigurationWriter extends MDDEClientConfigurationBase {
    /**
     * Serialize configuration to a string
     * @param config Configuration object
     * @return Serialized configuration
     * @throws JsonProcessingException
     */
    public String serializeConfiguration(MDDEClientConfiguration config) throws JsonProcessingException {
        Objects.requireNonNull(config);

        return getMapper().writeValueAsString(config);
    }

    /**
     * Write configuration to a file
     * @param config Configuration object
     * @param filePath Output file path. If the destination file already exits it will be overwritten
     * @throws IOException
     */
    public void writeConfiguration(MDDEClientConfiguration config, Path filePath)
            throws IOException {
        String strConfig = serializeConfiguration(config);
        Files.write(filePath, strConfig.getBytes(configurationCharset));
    }
}
