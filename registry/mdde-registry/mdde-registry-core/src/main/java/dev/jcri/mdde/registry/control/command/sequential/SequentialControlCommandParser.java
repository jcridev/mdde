package dev.jcri.mdde.registry.control.command.sequential;


import dev.jcri.mdde.registry.control.CommandParserControlBase;
import dev.jcri.mdde.registry.control.exceptions.CommandException;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerBase;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.store.RegistryStateCommandHandler;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;

/**
 * Process incoming registry state control commands
 * @param <T> Type of the serialized result
 */
public class SequentialControlCommandParser<T> extends CommandParserControlBase<T, List<Object>> {
    private final RegistryStateCommandHandler _stateCommandHandler;

    public SequentialControlCommandParser(RegistryStateCommandHandler stateCommandHandler,
                                          ResponseSerializerBase<T> serializer){
        super(serializer);
        Objects.requireNonNull(stateCommandHandler, "State commands handlers can't be null");
        _stateCommandHandler = stateCommandHandler;
    }

    @Override
    protected Set<String> processInitDefaultNodes() throws MddeRegistryException {
        return _stateCommandHandler.initializeDefaultNodes();
    }

    @Override
    protected Boolean processSetBenchmarkState() throws MddeRegistryException {
        return _stateCommandHandler.switchToBenchmark();
    }

    @Override
    protected Boolean processSetShuffleState() throws IOException, MddeRegistryException {
        return _stateCommandHandler.switchToShuffle();
    }

    @Override
    protected String processGetRegistryMode() throws MddeRegistryException {
        return _stateCommandHandler.getCurrentState().name();
    }

    @Override
    protected Boolean processReset() throws IOException, MddeRegistryException {
        return _stateCommandHandler.reset();
    }

    @Override
    protected Boolean syncDataFromRegistryToNodes() throws IOException {
        return _stateCommandHandler.syncRegistryToNodes();
    }

    @Override
    protected String processExecuteBenchmarkCommand(List<Object> arguments)
            throws MddeRegistryException {
        final EStateControlCommand thisCommand = EStateControlCommand.RUN_BENCHMARK;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var workloadId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_WORKLOAD_ID);
        var workloadWorkers = CommandParserHelper.sharedInstance().getPositionalArgumentAsInteger(arguments, thisCommand, ARG_WORKLOAD_WORKERS);
        return _stateCommandHandler.executeBenchmark(workloadId, workloadWorkers);
    }

    @Override
    protected boolean processLoadDataCommand(List<Object> arguments)
            throws MddeRegistryException {
        final EStateControlCommand thisCommand = EStateControlCommand.LOAD_DATA;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var workloadId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_WORKLOAD_ID);
        return _stateCommandHandler.generateData(workloadId);
    }

    @Override
    protected boolean processFlushAll() throws MddeRegistryException, IOException {
        return _stateCommandHandler.flushAll();
    }

    @Override
    protected String processCreateSnapshot(List<Object> arguments)
            throws CommandException, IOException {
        final EStateControlCommand thisCommand = EStateControlCommand.SNAPSHOT_CREATE;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var createAsDefault = CommandParserHelper.sharedInstance().getPositionalArgumentAsBoolean(arguments, thisCommand, ARG_DEFAULT_SNAPSHOT_FLAG);
        return _stateCommandHandler.createSnapshot(createAsDefault);
    }

    @Override
    protected boolean processLoadSnapshot(List<Object> arguments)
            throws CommandException, IOException{

        final EStateControlCommand thisCommand = EStateControlCommand.SNAPSHOT_LOAD;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var snapshot = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_SNAPSHOT_ID);
        return _stateCommandHandler.loadSnapshot(snapshot);
    }

    @Override
    protected BenchmarkStatus processGetBenchmarkState(){
        return _stateCommandHandler.retrieveLatestBenchmarkRunStatus();
    }

    @Override
    protected BenchmarkStatus processGetCounterfeitBenchmark() {
        return _stateCommandHandler.retrieveCounterfeitBenchmarkStatus();
    }
}
