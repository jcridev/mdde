package dev.jcri.mdde.registry.server.tcp.protocol;

import java.util.Objects;

public class BenchmarkContainerOut {
    private BenchmarkResultCodes result;
    private String returnValue;

    public BenchmarkContainerOut(BenchmarkResultCodes result, String returnValue) {
        Objects.requireNonNull(result, "Supply a valid result code");
        this.result = result;
        this.returnValue = returnValue;
    }

    public BenchmarkResultCodes getResult() {
        return result;
    }

    public String getReturnValue() {
        return returnValue;
    }
}
