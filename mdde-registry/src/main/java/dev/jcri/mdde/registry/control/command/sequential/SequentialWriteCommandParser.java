package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.CommandParser;
import dev.jcri.mdde.registry.control.WriteCommands;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.*;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static dev.jcri.mdde.registry.control.ExpectedCommandArgument.*;

public class SequentialWriteCommandParser<T> extends BaseSequentialCommandParser implements CommandParser<T, WriteCommands, List<Object>> {
    private final IWriteCommandHandler _writeCommandHandler;
    private final IResponseSerializer<T> _serializer;

    public SequentialWriteCommandParser(IWriteCommandHandler writeCommandHandler, IResponseSerializer<T> serializer) {
        Objects.requireNonNull(writeCommandHandler, "Write commands handlers can't be null");
        Objects.requireNonNull(serializer, "Serializer can't be null");
        _serializer = serializer;
        _writeCommandHandler = writeCommandHandler;
    }
    /**
     * Execute the specified query-like command
     * @param writeCommand WriteCommandHandler.Commands
     * @param arguments Key-value pairs
     */
    public final T runCommand(WriteCommands writeCommand, List<Object> arguments)
            throws UnknownRegistryCommandExceptions, MddeRegistryException {

        switch (writeCommand) {
            case INSERT_TUPLE:
                processInsertTupleCommand(arguments);
                break;
            case INSERT_TUPLE_BULK:
                processInsertTupleInBulkCommand(arguments);
                break;
            case DELETE_TUPLE:
                processDeleteTupleCommand(arguments);
                break;
            case FORM_FRAGMENT:
                processFormFragmentCommand(arguments);
                break;
            case APPEND_TO_FRAGMENT:
                processAppendToFragmentCommand(arguments);
                break;
            case REPLICATE_FRAGMENT:
                processReplicateFragmentCommand(arguments);
                break;
            case DELETE_FRAGMENT:
                processDeleteFragmentExemplar(arguments);
                break;
            case DESTROY_FRAGMENT:
                processDestroyFragment(arguments);
                break;
            case POPULATE_NODES:
                processPopulateNodes(arguments);
                break;
            default:
                throw new UnknownRegistryCommandExceptions(writeCommand.toString());
        }
        return _serializer.serialize("Done");
    }

    private void processInsertTupleCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        final var thisCommand = WriteCommands.INSERT_TUPLE;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int tupleIdPosition = 0 ;
        var tupleId = (String) Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_TUPLE_ID, tupleIdPosition));

        final int nodeIdPosition = 1;
        var nodeId = (String) Objects.requireNonNull(arguments.get(nodeIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_NODE_ID, nodeIdPosition));

        _writeCommandHandler.insertTuple(tupleId, nodeId);
    }

    private void processInsertTupleInBulkCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        final var thisCommand = WriteCommands.INSERT_TUPLE_BULK;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int tupleIdPosition = 0;
        var tupleIdsArg = Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_TUPLE_IDs, tupleIdPosition));
        if(!(tupleIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(getPositionalArgumentError(thisCommand.toString(), ARG_TUPLE_IDs, tupleIdPosition));
        }

        final int nodeIdPosition = 1;
        var nodeId = (String) Objects.requireNonNull(arguments.get(nodeIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_NODE_ID, nodeIdPosition));

        _writeCommandHandler.insertTuple((Set<String>) tupleIdsArg, nodeId);
    }

    private void processDeleteTupleCommand(final List<Object> arguments)
            throws ResponseSerializationException, UnknownEntityIdException, WriteOperationException {
        final var thisCommand = WriteCommands.DELETE_TUPLE;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int tupleIdPosition = 0;
        var tupleId = (String) Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_TUPLE_ID, tupleIdPosition));

        _writeCommandHandler.deleteTuple(tupleId);
    }

    private T processFormFragmentCommand(final List<Object> arguments)
            throws ResponseSerializationException, WriteOperationException, IllegalRegistryActionException, UnknownEntityIdException, DuplicateEntityRecordException {
        final var thisCommand = WriteCommands.FORM_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int tupleIdPosition = 0;
        var tupleIdsArg = Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_TUPLE_IDs, tupleIdPosition));
        if(!(tupleIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a collection %s ",
                    thisCommand.toString(), ARG_TUPLE_IDs));
        }

        final int fragmentIdPosition = 1;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_FRAGMENT_ID, fragmentIdPosition));

        final int nodeIdPosition = 2;
        var nodeId = (String) Objects.requireNonNull(arguments.get(nodeIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_NODE_ID, nodeIdPosition));

        return _serializer.serialize(_writeCommandHandler.formFragment((Set<String>) tupleIdsArg, fragmentId, nodeId));
    }

    private void processAppendToFragmentCommand(final List<Object> arguments)
            throws WriteOperationException, DuplicateEntityRecordException, UnknownEntityIdException {
        final var thisCommand = WriteCommands.APPEND_TO_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int tupleIdPosition = 0;
        var tupleId = (String) Objects.requireNonNull(arguments.get(tupleIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_TUPLE_ID, tupleIdPosition));

        final int fragmentIdPosition = 1;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_FRAGMENT_ID, fragmentIdPosition));

        _writeCommandHandler.appendTupleToFragment(tupleId, fragmentId);
    }

    private void processReplicateFragmentCommand(final List<Object> arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException {
        final var thisCommand = WriteCommands.REPLICATE_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int fragmentIdPosition = 0;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_FRAGMENT_ID, fragmentIdPosition));

        final int nodeAPosition = 1;
        var nodeIdA = (String) Objects.requireNonNull(arguments.get(nodeAPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_NODE_ID, nodeAPosition));

        final int nodeBPosition = 2;
        var nodeIdB = (String) Objects.requireNonNull(arguments.get(nodeBPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_NODE_ID_B, nodeBPosition));

        _writeCommandHandler.replicateFragment(fragmentId, nodeIdA, nodeIdB);
    }

    private void processDeleteFragmentExemplar(final List<Object> arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException {
        final var thisCommand = WriteCommands.DELETE_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int fragmentIdPosition = 0;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_FRAGMENT_ID, fragmentIdPosition));

        final int nodeAPosition = 1;
        var nodeIdA = (String) Objects.requireNonNull(arguments.get(nodeAPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_NODE_ID, nodeAPosition));

        _writeCommandHandler.deleteFragmentExemplar(fragmentId, nodeIdA);
    }

    private T processDestroyFragment(final List<Object> arguments)
            throws ResponseSerializationException, UnknownEntityIdException {
        final var thisCommand = WriteCommands.DESTROY_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int fragmentIdPosition = 0;
        var fragmentId = (String) Objects.requireNonNull(arguments.get(fragmentIdPosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_FRAGMENT_ID, fragmentIdPosition));

        return _serializer.serialize(_writeCommandHandler.deleteFragmentCompletely(fragmentId));
    }

    private T processPopulateNodes(final List<Object> arguments)
            throws IllegalRegistryActionException, ResponseSerializationException, WriteOperationException {
        final var thisCommand = WriteCommands.POPULATE_NODES;
        validateNotNullArguments(arguments, thisCommand.toString());

        final int nodePosition = 0;
        var nodeIdsArg = Objects.requireNonNull(arguments.get(nodePosition),
                getPositionalArgumentError(thisCommand.toString(), ARG_NODE_IDs, nodePosition));

        if(!(nodeIdsArg instanceof Set<?>)){
            throw new IllegalArgumentException(String.format("%s must be invoked with a set collection %s ",
                    thisCommand.toString(), ARG_NODE_IDs));
        }

        return _serializer.serialize(_writeCommandHandler.populateNodes((Set<String>)nodeIdsArg));
    }
}
