package dev.jcri.mdde.registry.configuration.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.nio.file.Paths;

@JsonPropertyOrder({
        YCSBConfig.YCSB_BIN_FIELD,
        YCSBConfig.YCSB_CLIENT_FILED
})
public class YCSBConfig {
    public static final String YCSB_BIN_FIELD = "bin";
    public static final String YCSB_CLIENT_FILED = "client";
    public static final String REGISTRY_TEMP_FOLDER_FIELD = "temp";
    /**
     * Folder where MDDE-registry creates its temporary files (such as temp YCSB configs)
     */
    private String temp;

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

    /**
     * Get path to the MDDE-Registry temporary files folder.
     * If it's null, returns path to {bin_location}/temp.
     * This should accessible for both YCSB binary and Registry process
     * @return
     */
    @JsonGetter(REGISTRY_TEMP_FOLDER_FIELD)
    public String getTemp() {
        if(temp != null && !temp.isBlank()){
            return temp;
        }
        return Paths.get(System.getProperty("user.dir"),"temp").toString();
    }
    @JsonSetter(REGISTRY_TEMP_FOLDER_FIELD)
    public void setTemp(String temp) {
        this.temp = temp;
    }
}
