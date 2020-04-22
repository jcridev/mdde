package dev.jcri.mdde.registry.server.tcp.protocol;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Incoming benchmark command container.
 */
public class BenchmarkContainerIn {
    /**
     * Command code.
     */
    private final BenchmarkOperationCodes _operation;
    /**
     * (optional) Arguments.
     */
    private final List<byte[]> _parameter;

    /**
     * Benchmark command container.
     * @param operation Byte operation code.
     * @param parameter (Optional) String encoded arguments. Max argument size can't 127 symbols.
     */
    public BenchmarkContainerIn(BenchmarkOperationCodes operation, List<byte[]> parameter) {
        Objects.requireNonNull(operation, "Supply a valid operation code");
        if(parameter != null && parameter.size() > Byte.MAX_VALUE - 3){
            throw new IllegalArgumentException(
                    String.format("Maximum number of parameters cant exceed %d", Byte.MAX_VALUE - 3));
        }
        this._operation = operation;
        if(parameter != null){
            _parameter = Collections.unmodifiableList(parameter);
        }
        else{
            this._parameter = null;
        }
    }

    /**
     * Get operation code.
     * @return BenchmarkOperationCodes (Byte enum).
     */
    public BenchmarkOperationCodes getOperation() {
        return _operation;
    }

    /**
     * (optional) String encoded arguments.
     * @return String encoded arguments or null if none supplied.
     */
    public List<byte[]> getParameter() {
        return _parameter;
    }

    /**
     * Get the number of the arguments received with the command.
     * @return Total number of the received arguments.
     */
    public byte numberOfArguments(){
        if(_parameter == null){
            return 0;
        }
        return (byte) _parameter.size();
    }

    /**
     * Description of the command.
     * @return Get string description of the received command.
     */
    @Override
    public String toString() {
        return String.format("Bench cmd: '%s' with args(%d)", _operation.toString(), numberOfArguments());
    }
}
