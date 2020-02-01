package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.shared.commands.containers.result.benchmark.BenchmarkStatus;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.io.IOException;
import java.util.Objects;

/**
 * Basic abstract router for the CONTROL commands
 * @param <TOut> Expected result type
 * @param <TArgs> Arguments container type
 */
public abstract class CommandParserControlBase<TOut, TArgs>
        implements ICommandParser<TOut, EStateControlCommand, TArgs> {

    protected final IResponseSerializer<TOut> _serializer;

    protected CommandParserControlBase(IResponseSerializer<TOut> serializer){
        Objects.requireNonNull(serializer, "Serializer can't be null");
        this._serializer = serializer;
    }

    public final TOut runCommand(EStateControlCommand command, TArgs arguments){
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

    protected abstract Boolean processSetBenchmarkState() throws MddeRegistryException;

    protected abstract Boolean processSetShuffleState() throws IOException;

    protected abstract Boolean processReset() throws IOException;

    protected abstract Boolean syncDataFromRegistryToNodes() throws IOException;

    protected abstract String processExecuteBenchmarkCommand(TArgs arguments) throws IllegalCommandArgumentException;

    protected abstract boolean processLoadDataCommand(TArgs arguments) throws IllegalCommandArgumentException;

    protected abstract boolean processFlushAll();

    protected abstract String processCreateSnapshot(TArgs arguments) throws IllegalCommandArgumentException, IOException;

    protected abstract boolean processLoadSnapshot(TArgs arguments) throws IllegalCommandArgumentException, IOException;

    protected abstract BenchmarkStatus processGetBenchmarkState();
}
