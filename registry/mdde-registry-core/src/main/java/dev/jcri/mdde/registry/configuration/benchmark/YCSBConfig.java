package dev.jcri.mdde.registry.configuration.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonPropertyOrder({
        YCSBConfig.YCSB_BIN_FIELD,
        YCSBConfig.YCSB_CLIENT_FILED
})
public class YCSBConfig {
    public static final String YCSB_BIN_FIELD = "bin";
    public static final String YCSB_CLIENT_FILED = "client";

    /**
     * Location pf the YCSB /bin folder
     */
    private String _ycsbBin;

    /**
     * YCSB client used for benchmark
     */
    private String _ycsbClient;

    @JsonGetter(YCSB_BIN_FIELD)
    public String getYcsbBin() {
        return _ycsbBin;
    }
    @JsonSetter(YCSB_BIN_FIELD)
    public void setYcsbBin(String ycsbBin) {
        this._ycsbBin = ycsbBin;
    }

    @JsonGetter(YCSB_CLIENT_FILED)
    public String getYcsbClient() {
        return _ycsbClient;
    }
    @JsonSetter(YCSB_CLIENT_FILED)
    public void setYcsbClient(String ycsbClient) {
        this._ycsbClient = ycsbClient;
    }
}
