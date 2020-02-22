package dev.jcri.mdde.registry.benchmark.ycsb.cli;

/**
 * Container for parsed YCSB output.
 * This is not an exhaustive list of returned values but rather only these that have value for us.
 *
 * Default values are -1. If a parameter wasn't found in the YCSB output, it will remain that.
 */
public class YCSBOutput {
    /**
     * RunTime(ms)
     */
    private long runtime = -1;
    /**
     * Throughput(ops/sec)
     */
    private double throughput = -1.0;

    private int readOperations = -1;
    private int readOperationsFailed = -1;
    /**
     * AverageLatency(us)
     */
    private double readLatencyAverage = -1.0;
    /**
     * MinLatency(us)
     */
    private int readLatencyMin = -1;
    /**
     * MaxLatency(us)
     */
    private int readLatencyMax = -1;

    private int updateOperations = -1;
    private int updateOperationsFailed = -1;
    /**
     * AverageLatency(us)
     */
    private double updateLatencyAverage = -1.0;
    /**
     * MinLatency(us)
     */
    private int updateLatencyMin = -1;
    /**
     * MaxLatency(us)
     */
    private int updateLatencyMax = -1;

    private int insertOperations = -1;
    private int insertOperationsFailed = -1;
    /**
     * AverageLatency(us)
     */
    private double insertLatencyAverage = -1;
    /**
     * MinLatency(us)
     */
    private int insertLatencyMin = -1;
    /**
     * MaxLatency(us)
     */
    private int insertLatencyMax = -1;

    public long getRuntime() {
        return runtime;
    }

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    public int getReadOperations() {
        return readOperations;
    }

    public void setReadOperations(int readOperations) {
        this.readOperations = readOperations;
    }

    public int getReadOperationsFailed() {
        return readOperationsFailed;
    }

    public void setReadOperationsFailed(int readOperationsFailed) {
        this.readOperationsFailed = readOperationsFailed;
    }

    public double getReadLatencyAverage() {
        return readLatencyAverage;
    }

    public void setReadLatencyAverage(double readLatencyAverage) {
        this.readLatencyAverage = readLatencyAverage;
    }

    public int getReadLatencyMin() {
        return readLatencyMin;
    }

    public void setReadLatencyMin(int readLatencyMin) {
        this.readLatencyMin = readLatencyMin;
    }

    public int getReadLatencyMax() {
        return readLatencyMax;
    }

    public void setReadLatencyMax(int readLatencyMax) {
        this.readLatencyMax = readLatencyMax;
    }

    public int getUpdateOperations() {
        return updateOperations;
    }

    public void setUpdateOperations(int updateOperations) {
        this.updateOperations = updateOperations;
    }

    public int getUpdateOperationsFailed() {
        return updateOperationsFailed;
    }

    public void setUpdateOperationsFailed(int updateOperationsFailed) {
        this.updateOperationsFailed = updateOperationsFailed;
    }

    public double getUpdateLatencyAverage() {
        return updateLatencyAverage;
    }

    public void setUpdateLatencyAverage(double updateLatencyAverage) {
        this.updateLatencyAverage = updateLatencyAverage;
    }

    public int getUpdateLatencyMin() {
        return updateLatencyMin;
    }

    public void setUpdateLatencyMin(int updateLatencyMin) {
        this.updateLatencyMin = updateLatencyMin;
    }

    public int getUpdateLatencyMax() {
        return updateLatencyMax;
    }

    public void setUpdateLatencyMax(int updateLatencyMax) {
        this.updateLatencyMax = updateLatencyMax;
    }

    public int getInsertOperations() {
        return insertOperations;
    }

    public void setInsertOperations(int insertOperations) {
        this.insertOperations = insertOperations;
    }

    public int getInsertOperationsFailed() {
        return insertOperationsFailed;
    }

    public void setInsertOperationsFailed(int insertOperationsFailed) {
        this.insertOperationsFailed = insertOperationsFailed;
    }

    public double getInsertLatencyAverage() {
        return insertLatencyAverage;
    }

    public void setInsertLatencyAverage(double insertLatencyAverage) {
        this.insertLatencyAverage = insertLatencyAverage;
    }

    public int getInsertLatencyMin() {
        return insertLatencyMin;
    }

    public void setInsertLatencyMin(int insertLatencyMin) {
        this.insertLatencyMin = insertLatencyMin;
    }

    public int getInsertLatencyMax() {
        return insertLatencyMax;
    }

    public void setInsertLatencyMax(int insertLatencyMax) {
        this.insertLatencyMax = insertLatencyMax;
    }
}
