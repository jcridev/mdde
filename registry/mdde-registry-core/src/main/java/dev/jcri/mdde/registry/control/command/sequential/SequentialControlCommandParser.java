package dev.jcri.mdde.registry.control.command.sequential;


import dev.jcri.mdde.registry.control.CommandParserControlBase;
import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.store.RegistryStateCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;

/**
 * Process incoming registry state control commands
 * @param <T> Type of the serialized result
 */
public class SequentialControlCommandParser<T> extends CommandParserControlBase<T, List<Object>> {
    private final RegistryStateCommandHandler _stateCommandHandler;

    public SequentialControlCommandParser(RegistryStateCommandHandler stateCommandHandler,
                                          IResponseSerializer<T> serializer){
        super(serializer);
        Objects.requireNonNull(stateCommandHandler, "State commands handlers can't be null");
        _stateCommandHandler = stateCommandHandler;
    }

    protected Boolean processSetBenchmarkState() throws MddeRegistryException {
        return _stateCommandHandler.switchToBenchmark();
    }

    protected Boolean processSetShuffleState() throws IOException {
        return _stateCommandHandler.switchToShuffle();
    }

    protected Boolean processReset() throws IOException{
        return _stateCommandHandler.reset();
    }

    protected Boolean syncDataFromRegistryToNodes() throws IOException {
        return _stateCommandHandler.syncRegistryToNodes();
    }

    protected String processExecuteBenchmarkCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EStateControlCommand thisCommand = EStateControlCommand.RUN_BENCHMARK;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var workloadId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_WORKLOAD_ID);
        return _stateCommandHandler.executeBenchmark(workloadId);
    }

    protected boolean processLoadDataCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EStateControlCommand thisCommand = EStateControlCommand.LOAD_DATA;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var workloadId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_WORKLOAD_ID);
        return _stateCommandHandler.generateData(workloadId);
    }

    protected boolean processFlushAll(){
        return _stateCommandHandler.flushAll();
    }

    protected String processCreateSnapshot(List<Object> arguments)
            throws IllegalCommandArgumentException, IOException {
        final EStateControlCommand thisCommand = EStateControlCommand.SNAPSHOT_CREATE;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var createAsDefault = CommandParserHelper.sharedInstance().getPositionalArgumentAsBoolean(arguments, thisCommand, ARG_DEFAULT_SNAPSHOT_FLAG);
        return _stateCommandHandler.createSnapshot(createAsDefault);
    }

    protected boolean processLoadSnapshot(List<Object> arguments)
            throws IllegalCommandArgumentException, IOException{

        final EStateControlCommand thisCommand = EStateControlCommand.SNAPSHOT_LOAD;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var snapshot = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_SNAPSHOT_ID);
        return _stateCommandHandler.loadSnapshot(snapshot);
    }

    protected BenchmarkStatus processGetBenchmarkState(){
        return _stateCommandHandler.retrieveLatestBenchmarkRunStatus();
    }
}
