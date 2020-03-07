package dev.jcri.mdde.registry.shared.commands.containers.result.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.beans.Transient;

/**
 * Benchmark statistics of a fragment
 */
public class BenchmarkFragmentStats {
    public static final String READS_FIELD = "r";
    /**
     * Count of the read operations within the benchmark run
     */
    private Integer _readCount;

    /**
     * Default constructor
     */
    public BenchmarkFragmentStats(){}

    /**
     * Constructor
     * @param initialRead Initial number of reads
     */
    public BenchmarkFragmentStats(int initialRead){
        _readCount = initialRead;
    }

    @JsonGetter(READS_FIELD)
    public Integer getReadCount() {
        return _readCount;
    }
    @JsonSetter(READS_FIELD)
    public void setReadCount(Integer readCount) {
        this._readCount = readCount;
    }

    /**
     * Increase read count by 1
     * @return New read count value
     */
    @Transient
    @JsonIgnore
    public int incrementReads(){
        this._readCount += 1;
        return this._readCount;
    }
}
