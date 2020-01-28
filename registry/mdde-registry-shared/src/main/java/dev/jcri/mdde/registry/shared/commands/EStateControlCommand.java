package dev.jcri.mdde.registry.shared.commands;

import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.ARG_WORKLOAD_ID;

/**
 * Registry state switch
 */
public enum  EStateControlCommand implements ICommand {
    /**
     * Set Registry into the benchmark mode.
     * Shuffle is not allowed, benchmark interface is active.
     */
    SET_BENCHMARK("PREPBENCHMARK"),
    /**
     * Execute benchmark and return the resulted metrics
     */
    RUN_BENCHMARK("BENCHRUN", new ArrayList<ExpectedCommandArgument>(){{add(ARG_WORKLOAD_ID); }}),
    /**
     * Generate data for the benchmark
     */
    LOAD_DATA("BENCHLOAD", new ArrayList<ExpectedCommandArgument>(){{add(ARG_WORKLOAD_ID); }}),
    /**
     * Set Registry into the data shuffle mode.
     * Benchmark interface is disabled.
     */
    SET_SHUFFLE("SHUFFLE"),
    /**
     * Execute the current shuffle query on the actual data nodes.
     * All of the actions that were performed in the registry must've been placed in the data shuffle queue, when this
     * command is executed operations from the shuffle queue are executed one by one in the data nodes, thus
     * synchronizing the state of the data nodes with the registry.
     */
    RUN_SHUFFLE("SYNCNODES"),
    /**
     * Reset the state of the environment.
     * Following must happen after this command is executed:
     *  1. Flush Registry
     *  2. Flush Data nodes
     *  3. Populate node records within the registry according to the registry configuration
     *  4. Populate data nodes with tuples and registry with records about these tuples
     *
     */
    RESET("RESET"),
    /**
     * Clears the registry, data nodes and any temporary files that were potentially created during the runtime; thus
     * returning the environment into the default state.
     */
    FLUSHALL("FLUSHALL");

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

    private static Map<String, EStateControlCommand> _commandsMap = Arrays.stream(EStateControlCommand.values())
            .collect(Collectors.toMap(e -> e._command, e -> e));

    public static EStateControlCommand getCommandTag(String tag) throws NoSuchElementException {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        EStateControlCommand command = _commandsMap.get(tag);
        if(command == null){
            throw new NoSuchElementException(tag);
        }
        return command;
    }
}
