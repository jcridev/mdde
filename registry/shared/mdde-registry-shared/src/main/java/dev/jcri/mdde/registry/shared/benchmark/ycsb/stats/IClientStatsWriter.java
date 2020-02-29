package dev.jcri.mdde.registry.shared.benchmark.ycsb.stats;

import java.io.Closeable;
import java.time.LocalDateTime;

/**
 * Statistics gatherer interface for a YCSB client
 */
public interface IClientStatsWriter extends Closeable {
    /**
     * Add a READ operation record to the statistics, with the .now() timestamp
     * @param tupleId Tuple ID that was read
     * @param nodeId Node ID from which tuple was read
     * @param success True - action was a success
     */
    void addReadToLog(String tupleId, String nodeId, boolean success);

    /**
     * Add a READ operation record to the statistics
     * @param tupleId Tuple ID that was read
     * @param nodeId Node ID from which tuple was read
     * @param success True - action was a success
     * @param timestamp Timestamp
     */
    void addReadToLog(String tupleId, String nodeId, boolean success, LocalDateTime timestamp);
}
