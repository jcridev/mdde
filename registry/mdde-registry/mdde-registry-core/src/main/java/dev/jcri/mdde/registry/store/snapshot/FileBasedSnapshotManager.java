package dev.jcri.mdde.registry.store.snapshot;

import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.store.IStoreManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.UUID;

/**
 * Snapshots manager with an SQLLite based catalog.
 */
public class FileBasedSnapshotManager extends StoreSnapshotManagerBase {
    private static final Logger logger = LogManager.getLogger(FileBasedSnapshotManager.class);

    private static final String _snapshotsCatalogFileName = "mdde-snapshots.db";

    public FileBasedSnapshotManager(String snapshotsDirectory,
                                    IDataShuffler dataShuffler,
                                    IStoreManager registryManager) {
        super(snapshotsDirectory, dataShuffler, registryManager);
    }

    private String getSnapshotsDbPath() throws NotDirectoryException {
        final String snapshotsPath = getSnapshotsDirPath();
        return Paths.get(snapshotsPath, _snapshotsCatalogFileName).toString();
    }

    /**
     * Get connection to the SQLLite catalog of the snapshots
     * @return SQLite connection
     * @throws SQLException
     * @throws NotDirectoryException
     */
    private Connection getSnapshotsCatalogConnection()
            throws SQLException, NotDirectoryException {
        var pathToDb = getSnapshotsDbPath();
        var fileAlreadyExists = new File(pathToDb).exists();
        var connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", pathToDb));
        if(fileAlreadyExists){
            return connection;
        }
        else{
            var createSnapsTable =  "CREATE TABLE IF NOT EXISTS snapshots (\n" +
                    "    snapshot_id TEXT NOT NULL PRIMARY KEY,\n" +
                    "    created DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')) NOT NULL\n" +
                    ");";

            var createDefSnTable =  "CREATE TABLE IF NOT EXISTS default_snapshot (\n" +
                    "    id INTEGER PRIMARY KEY CHECK (id = 0),\n" +
                    "    snapshot_id TEXT NOT NULL,\n" +
                    "    FOREIGN KEY (snapshot_id)\n" +
                    "       REFERENCES snapshots (snapshot_id)\n" +
                    ");";

            try(Statement statement = connection.createStatement()) {
                statement.executeUpdate(createSnapsTable);
                statement.executeUpdate(createDefSnTable);
            }
        }
        return connection;
    }

    private void insertNewSnapshotId(String snapshotId, boolean asDefault)
            throws SQLException, NotDirectoryException {
        try(var connection = getSnapshotsCatalogConnection()){

            var newSnapIdInsert = String.format("INSERT INTO snapshots(snapshot_id) VALUES ('%s');", snapshotId);
            Statement statement = connection.createStatement();
            statement.executeUpdate(newSnapIdInsert);
            if(asDefault){
                var newDefSnapIdInsert =
                        String.format(  "INSERT INTO default_snapshot(id, snapshot_id)\n" +
                                "VALUES (0, '%s')\n" +
                                "ON CONFLICT(id)\n" +
                                "    DO UPDATE SET snapshot_id=excluded.snapshot_id;", snapshotId);
                statement.executeUpdate(newDefSnapIdInsert);
            }
        }
    }

    /**
     * Retrieve ID of default snapshot.
     * @return ID of the default snapshot if exists, null otherwise.
     * @throws IOException Failed to read the ID.
     */
    public String getDefaultSnapshotId() throws IOException {
        try(var connection = getSnapshotsCatalogConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT snapshot_id FROM default_snapshot WHERE id=0;");
            if(rs.next()){
                return rs.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            var errorText = "Error accessing snapshots db";
            logger.error(errorText, e);
            throw new IOException(errorText, e);
        }
    }

    /**
     * Delete all snapshots from the configured snapshot folder.
     * @return True - Success.
     * @throws IOException Error removing snapshots.
     */
    public boolean flushSnapshots() throws IOException {
        var pathToDb = getSnapshotsDbPath();
        if(!new File(pathToDb).exists()){
            return true;
        }
        // Delete snapshot files
        try(var connection = getSnapshotsCatalogConnection()){
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT snapshot_id FROM snapshots;");
            final String snapshotsPath = getSnapshotsDirPath();
            while(rs.next())
            { // Remove known snapshot files
                try{
                    var snapFilenamePrefix = rs.getString(1);
                    var pathRegistry = Paths.get(snapshotsPath, snapFilenamePrefix + _registryDumpFilePostfix);
                    var pathData = Paths.get(snapshotsPath, snapFilenamePrefix + _dataNodeDumpFilePostfix);
                    Files.deleteIfExists(pathRegistry);
                    Files.deleteIfExists(pathData);
                } catch (IOException ex) {
                    logger.error("Error flushing a snapshot", ex);
                }
            }
        } catch (SQLException ex) {
            logger.error("Unable to flush snapshots, SQLite error.", ex);
            throw new IOException("SQLite error", ex);
        }
        // Delete the database
        Files.deleteIfExists(Paths.get(pathToDb));
        return true;
    }

    /**
     * Crete full snapshot of the Registry with Data and dump them in the snapshots directory.
     * @param isDefault If True - newly created snapshot is set as Default and will be used during the RESET execution.
     * @return Snapshot ID.
     * @throws IOException
     */
    public String createSnapshot(boolean isDefault) throws IOException {
        try {
            final String snapshotsPath = getSnapshotsDirPath();
            // Generate snapshot ID (used as filenames)
            final String snapFilenamePrefix = UUID.randomUUID().toString().replace("-", "");
            final String snapRegistryFile = Paths.get(snapshotsPath, snapFilenamePrefix + _registryDumpFilePostfix)
                    .toString();
            final String snapDataNodesFile = Paths.get(snapshotsPath, snapFilenamePrefix + _dataNodeDumpFilePostfix)
                    .toString();
            // Dump registry into the file
            _registryStoreManager.dumpToFile(snapRegistryFile, true);
            // Dump data from all of the nodes into the file
            _dataShuffler.dumpToFile(snapDataNodesFile, true);
            // Record the snapshot in the catalog
            insertNewSnapshotId(snapFilenamePrefix, isDefault);

            return snapFilenamePrefix;
        }
        catch (SQLException e){
            var errorText = "Failed to save info about a new snapshot";
            logger.error(errorText, e);
            throw new IOException(errorText, e);
        }
        catch (Exception ex){
            logger.error("Failed creating snapshot", ex);
            throw ex;
        }
    }

    /**
     * Load snapshot from a file.
     * @param snapshotId ID of the snapshot to load.
     * @return True - snapshot was restored successfully.
     * @throws IOException
     */
    public boolean loadSnapshot(String snapshotId) throws IOException{

        try {
            if (snapshotId == null || snapshotId.isBlank()) {
                throw new IllegalArgumentException("Snapshot ID is not set");
            }
            final String snapshotsPath = getSnapshotsDirPath();
            var snapDirFile = new File(snapshotsPath);
            if (!snapDirFile.exists() || snapDirFile.isFile()) {
                throw new NotDirectoryException(snapshotsPath);
            }
            final String snapRegistryFile = Paths.get(snapshotsPath, snapshotId + _registryDumpFilePostfix)
                    .toString();
            final String snapDataNodesFile = Paths.get(snapshotsPath, snapshotId + _dataNodeDumpFilePostfix)
                    .toString();

            if (!new File(snapRegistryFile).isFile()) {
                throw new FileNotFoundException(snapRegistryFile);
            }
            if (!new File(snapDataNodesFile).isFile()) {
                throw new FileNotFoundException(snapDataNodesFile);
            }

            var dataRestored = _dataShuffler.restoreFromFile(snapDataNodesFile);
            var registryRestored = _registryStoreManager.restoreFromFile(snapRegistryFile);

            return dataRestored && registryRestored;
        }
        catch (Exception ex){
            logger.error("Failed loading snapshot", ex);
            throw ex;
        }
    }
}
