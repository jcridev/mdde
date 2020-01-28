package dev.jcri.mdde.registry.control.command.sequential;


import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkRunResult;
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
public class SequentialControlCommandParser<T> extends BaseSequentialCommandParser
        implements ICommandParser<T, EStateControlCommand, List<Object>> {
    private final RegistryStateCommandHandler _stateCommandHandler;
    private final IResponseSerializer<T> _serializer;

    public SequentialControlCommandParser(RegistryStateCommandHandler stateCommandHandler,
                                          IResponseSerializer<T> serializer){
        Objects.requireNonNull(stateCommandHandler, "State commands handlers can't be null");
        Objects.requireNonNull(serializer, "Serializer can't be null");
        _serializer = serializer;
        _stateCommandHandler = stateCommandHandler;
    }

    public T runCommand(EStateControlCommand command, List<Object> arguments) {
        try {
            switch (command) {
                case SET_BENCHMARK:
                    return _serializer.serialize(processSetBenchmarkState());
                case RUN_BENCHMARK:
                    return _serializer.serialize(processExecuteBenchmarkCommand(arguments));
                case LOAD_DATA:
                    return _serializer.serialize(processLoadDataCommand(arguments));
                case SET_SHUFFLE:
                    return _serializer.serialize(processSetShuffleState());
                case RUN_SHUFFLE:
                    return _serializer.serialize(syncDataFromRegistryToNodes());
                case RESET:
                    return _serializer.serialize(processReset());
                case FLUSHALL:
                    return _serializer.serialize(processFlushAll());
                case SNAPSHOT_CREATE:
                    return _serializer.serialize(processCreateSnapshot(arguments));
                case SNAPSHOT_LOAD:
                    return _serializer.serialize(processLoadSnapshot(arguments));
                case GET_BENCHMARK:
                    return _serializer.serialize(processGetBenchmarkState());
                default:
                    throw new UnknownRegistryCommandExceptions(command.toString());
            }
        }
        catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }

    private Boolean processSetBenchmarkState() throws MddeRegistryException {
        return _stateCommandHandler.switchToBenchmark();
    }

    private Boolean processSetShuffleState() throws IOException {
        return _stateCommandHandler.switchToShuffle();
    }

    private Boolean processReset() throws IOException{
        return _stateCommandHandler.reset();
    }

    private Boolean syncDataFromRegistryToNodes() throws IOException {
        return _stateCommandHandler.syncRegistryToNodes();
    }

    private BenchmarkRunResult processExecuteBenchmarkCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EStateControlCommand thisCommand = EStateControlCommand.RUN_BENCHMARK;
        validateNotNullArguments(arguments, thisCommand.toString());

        var workloadId = getPositionalArgumentAsString(arguments, thisCommand, ARG_WORKLOAD_ID);
        return _stateCommandHandler.executeBenchmark(workloadId);
    }

    private boolean processLoadDataCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EStateControlCommand thisCommand = EStateControlCommand.LOAD_DATA;
        validateNotNullArguments(arguments, thisCommand.toString());

        var workloadId = getPositionalArgumentAsString(arguments, thisCommand, ARG_WORKLOAD_ID);
        return _stateCommandHandler.generateData(workloadId);
    }

    private boolean processFlushAll(){
        return _stateCommandHandler.flushAll();
    }

    private String processCreateSnapshot(List<Object> arguments)
            throws IllegalCommandArgumentException, IOException {
        final EStateControlCommand thisCommand = EStateControlCommand.SNAPSHOT_CREATE;
        validateNotNullArguments(arguments, thisCommand.toString());

        var createAsDefault = getPositionalArgumentAsBoolean(arguments, thisCommand, ARG_DEFAULT_SNAPSHOT_FLAG);
        return _stateCommandHandler.createSnapshot(createAsDefault);
    }

    private boolean processLoadSnapshot(List<Object> arguments)
            throws IllegalCommandArgumentException, IOException{

        final EStateControlCommand thisCommand = EStateControlCommand.SNAPSHOT_LOAD;
        validateNotNullArguments(arguments, thisCommand.toString());

        var snapshot = getPositionalArgumentAsString(arguments, thisCommand, ARG_SNAPSHOT_ID);
        return _stateCommandHandler.loadSnapshot(snapshot);
    }

    private String processGetBenchmarkState(){
        // TODO:
        return null;
    }
}
