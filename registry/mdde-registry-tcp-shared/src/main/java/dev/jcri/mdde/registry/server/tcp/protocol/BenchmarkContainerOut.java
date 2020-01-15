package dev.jcri.mdde.registry.server.tcp.protocol;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BenchmarkContainerOut {
    private BenchmarkResultCodes result;
    private List<byte[]> returnValue;

    public BenchmarkContainerOut(BenchmarkResultCodes result, List<byte[]> values) {
        Objects.requireNonNull(result, "Supply a valid result code");
        if(values != null && values.size() > Byte.MAX_VALUE - 3){
            throw new IllegalArgumentException(
                    String.format("Maximum number of parameters cant exceed %d", Byte.MAX_VALUE - 3));
        }
        this.result = result;
        if(values != null){
            returnValue = Collections.unmodifiableList(values);
        }
        else{
            this.returnValue = null;
        }
    }

    public BenchmarkResultCodes getResult() {
        return result;
    }

    public List<byte[]> getReturnValue() {
        return returnValue;
    }

    public byte numberOfValues(){
        if(returnValue == null){
            return 0;
        }
        return (byte) returnValue.size();
    }
}
