package dev.jcri.mdde.registry.store;

import java.io.IOException;

/**
 * Interface that must be implemented by the registry store implementation in order for the registry to be able to
 * perform management operations such as making snapshots, restoration from a file
 */
public interface IStoreManager {
    /**
     * Save all of the current data nodes data snapshots to a file.
     * Database specific file, meant for later restore by restoreFromFile(...)
     * @param pathToFile Full file name
     * @param overwrite Overwrite if the file already exits
     * @return True - data was saved successfully
     */
    boolean dumpToFile(String pathToFile, boolean overwrite) throws IOException;

    /**
     * Restore data from a file
     * @param pathToFile Full file name
     * @return True - data was restored successfully
     */
    boolean restoreFromFile(String pathToFile) throws IOException;

    /**
     * Erase all records from registry store
     * @return True erase was successful
     */
    boolean flushAllData();
}
