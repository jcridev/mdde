package dev.jcri.mdde.registry.utility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ResourcesTools {
    /**
     * Copy a file from the resources to the local file system
     * @param resourceName Name of the resource
     * @param destination Path to the destination (destination is overwritten if exists)
     * @throws IOException
     */
    public static void copyResourceToFileSystem(String resourceName, Path destination) throws IOException {
        try(InputStream resourceSourceStream = ResourcesTools.class.getResourceAsStream(resourceName)){
            Files.copy(resourceSourceStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
