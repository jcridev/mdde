package dev.jcri.mdde.registry.server.tcp.protocol;

import java.util.Objects;

public class BenchmarkContainerIn {
    private BenchmarkOperationCodes _operation;
    private String _parameter;

    public BenchmarkContainerIn(BenchmarkOperationCodes operation, String parameter) {
        Objects.requireNonNull(operation, "Supply a valid operation code");
        this._operation = operation;
        this._parameter = parameter;
    }

    public BenchmarkOperationCodes getOperation() {
        return _operation;
    }

    public String getParameter() {
        return _parameter;
    }
}
