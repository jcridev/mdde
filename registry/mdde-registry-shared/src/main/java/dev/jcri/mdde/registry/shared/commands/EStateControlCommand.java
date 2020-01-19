package dev.jcri.mdde.registry.shared.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registry state switch
 */
public enum  EStateControlCommand implements ICommand {
    /**
     * Set Registry into the benchmark mode.
     * Shuffle is not allowed, benchmark interface is active.
     */
    SET_BENCHMARK("BENCHMARK"),
    /**
     * Set Registry into the data shuffle mode.
     * Benchmark interface is disabled.
     */
    SET_SHUFFLE("SHUFFLE");

    private final String _command;
    private final List<ExpectedCommandArgument> _expectedArguments;

    EStateControlCommand(final String command) {
        this(command, new ArrayList<>());
    }

    EStateControlCommand(final String command, final List<ExpectedCommandArgument> args) {
        Objects.requireNonNull(args);
        this._command = command;
        _expectedArguments = args;
    }

    @Override
    public String getCommand() {
        return _command;
    }

    @Override
    public List<ExpectedCommandArgument> getExpectedArguments() {
        return _expectedArguments;
    }
}
