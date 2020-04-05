package dev.jcri.mdde.registry.shared.commands;

import java.util.*;
import java.util.stream.Collectors;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;

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
    RUN_BENCHMARK("RUNBENCH", new ArrayList<ExpectedCommandArgument>(){
        {
            add(ARG_WORKLOAD_ID);
            add(ARG_WORKLOAD_WORKERS);
        }
    }),

    /**
     * Get the status of the benchmark. If the benchmark run was finished, returns the results
     */
    GET_BENCHMARK("BENCHSTATE"),
    /**
     * Generate data for the benchmark
     */
    LOAD_DATA("BENCHLOAD", new ArrayList<ExpectedCommandArgument>(){{add(ARG_WORKLOAD_ID); }}),
    /**
     * Set Registry into the data shuffle mode.
     * Benchmark interface is disabled.
     */
    SET_SHUFFLE("PREPSHUFFLE"),
    /**
     * Execute the current shuffle query on the actual data nodes.
     * All of the actions that were performed in the registry must've been placed in the data shuffle queue, when this
     * command is executed operations from the shuffle queue are executed one by one in the data nodes, thus
     * synchronizing the state of the data nodes with the registry.
     */
    RUN_SHUFFLE("RUNSHUFFLE"),
    /**
     * Get the current state of the registry (Benchmark or Shuffle)
     */
    GET_REGISTRY_MODE("REGMODE"),
    /**
     * Initialize *default* nodes from the registry configuration file. Fails if the default nodes were already populated
     */
    INIT_NODES("INITNODES"),
    /**
     * Reset the state of the environment.
     * Following must happen after this command is executed:
     *  1. Flush Registry
     *  2. Flush Data nodes
     *  3. Populate registry
     *      3.1. If there is a default snapshot:
     *          3.1.1. Restore registry from the snapshot
     *          3.1.2. Restore data nodes from the snapshot
     *      3.2. If the is no default snapshot:
     *          3.2.1. Populate node records within the registry according to the registry configuration
     *          - [user] Data nodes remain empty. Data LOAD command must be executed with the appropriate workload arg
     *          - [user] Default snapshot should be created
     */
    RESET("RESET"),
    /**
     * Clears the registry, data nodes and any temporary files that were potentially created during the runtime; thus
     * returning the environment into the default state.
     */
    FLUSHALL("FLUSHALL"),
    /**
     * Save the current state of the registry and data nodes into files
     * If Default Snapshot flag is set to True, this snapshot will be used next time RESET is executed.
     * Returns newly created Snapshot ID
     */
    SNAPSHOT_CREATE("SNAPSAVE", new ArrayList<ExpectedCommandArgument>(){{add(ARG_DEFAULT_SNAPSHOT_FLAG); }}),
    /**
     * Explicitly load a snapshot with an ID.
     */
    SNAPSHOT_LOAD("SNAPLOAD", new ArrayList<ExpectedCommandArgument>(){{add(ARG_SNAPSHOT_ID); }});

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
