package dev.jcri.mdde.registry.store.snapshot;

import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.store.IStoreManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;

public abstract class StoreSnapshotManagerBase {
    private static final Logger logger = LogManager.getLogger(StoreSnapshotManagerBase.class);

    /**
     * Postfix for the registry records snapshot file
     */
    protected static final String _registryDumpFilePostfix = "-registry.dmp";
    /**
     * Postfix for the data nodes data snapshot file
     */
    protected static final String _dataNodeDumpFilePostfix = "-data.dmp";

    protected final IDataShuffler _dataShuffler;
    protected final IStoreManager _registryStoreManager;

    private final String _snapshotsDirectory;

    public StoreSnapshotManagerBase(String snapshotsDirectory,
                                    IDataShuffler dataShuffler,
                                    IStoreManager registryManager){
        _snapshotsDirectory = Paths.get(snapshotsDirectory).toAbsolutePath().normalize().toString();
        _dataShuffler = dataShuffler;
        _registryStoreManager = registryManager;
    }

    /**
     * Get fully resolved, normalized path to the snapshots directory. If the directory doesn't exist, it will be created.
     * @return fully resolved, normalized path to the snapshots directory
     * @throws NotDirectoryException Thrown if there is a file with the same name as the configured snapshots directory
     */
    protected String getSnapshotsDirPath() throws NotDirectoryException {
        var snapDirFile = new File(_snapshotsDirectory);
        if (snapDirFile.exists() && snapDirFile.isFile()) {
            throw new NotDirectoryException(_snapshotsDirectory);
        }
        boolean mkdirsRes = snapDirFile.mkdirs();
        logger.trace("New snapshot directory was created: {}", mkdirsRes);
        return _snapshotsDirectory;
    }

    /**
     * Crete full snapshot of the Registry with Data and dump them in the snapshots directory
     * @param isDefault If True - newly created snapshot is set as Default and will be used during the RESET execution
     * @return ID of a newly create snapshot
     * @throws IOException Error creating a snapshot
     */
    public abstract String createSnapshot(boolean isDefault) throws IOException;

    /**
     * Load snapshot from a file
     * @param snapshotId ID of the snapshot to load
     * @return True - snapshot was restored successfully
     * @throws IOException Error restoring snapshot
     */
    public abstract boolean loadSnapshot(String snapshotId) throws IOException;

    /**
     * Delete all currently known snapshots
     * @return All currently known snapshots were removed
     * @throws IOException Error removing current snapshots
     */
    public abstract boolean flushSnapshots() throws IOException;

    /**
     * Get ID of the default snapshot
     * @return ID of the default snapshot. Null if there is no default snapshot.
     */
    public abstract String getDefaultSnapshotId() throws IOException;
}
