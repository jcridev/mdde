package dev.jcri.mdde.registry.shared.commands.containers.result.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.beans.Transient;


public class BenchmarkFragmentStats {
    public static final String FRAGMENT_ID = "id";
    public static final String READS_FIELD = "reads";
    /**
     * Fragment ID
     */
    private String _fragmentId;
    /**
     * Count of the read operations within the benchmark run
     */
    private Integer _readCount;

    /**
     * Default constructor
     */
    public BenchmarkFragmentStats(){}

    public BenchmarkFragmentStats(String fragmentId, int initialRead){
        _fragmentId = fragmentId;
        _readCount = initialRead;
    }

    @JsonGetter(FRAGMENT_ID)
    public String getFragmentId() {
        return _fragmentId;
    }
    @JsonSetter(FRAGMENT_ID)
    public void setFragmentId(String fragmentId) {
        this._fragmentId = fragmentId;
    }

    @JsonGetter(READS_FIELD)
    public Integer getReadCount() {
        return _readCount;
    }
    @JsonSetter(READS_FIELD)
    public void setReadCount(Integer readCount) {
        this._readCount = readCount;
    }

    @Transient
    @JsonIgnore
    public int incrementReads(){
        this._readCount += 1;
        return this._readCount;
    }
}
