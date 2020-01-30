package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.server.responders.WriteCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.store.exceptions.*;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;

import java.util.List;
import java.util.Objects;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;


public class SequentialWriteCommandParser<T> extends BaseSequentialCommandParser implements ICommandParser<T, EWriteCommand, List<Object>> {
    private final WriteCommandResponder _writeCommandHandler;
    private final IResponseSerializer<T> _serializer;

    public SequentialWriteCommandParser(WriteCommandResponder writeCommandHandler, IResponseSerializer<T> serializer) {
        Objects.requireNonNull(writeCommandHandler, "Write commands handlers can't be null");
        Objects.requireNonNull(serializer, "Serializer can't be null");
        _serializer = serializer;
        _writeCommandHandler = writeCommandHandler;
    }
    /**
     * Execute the specified query-like command
     * @param EWriteCommand WriteCommandHandler.Commands
     * @param arguments Key-value pairs
     */
    public final T runCommand(EWriteCommand EWriteCommand, List<Object> arguments) {
        try {
            switch (EWriteCommand) {
                case INSERT_TUPLE:
                    return processInsertTupleCommand(arguments);
                case INSERT_TUPLE_BULK:
                    return processInsertTupleInBulkCommand(arguments);
                case DELETE_TUPLE:
                    return processDeleteTupleCommand(arguments);
                case FORM_FRAGMENT:
                    return processFormFragmentCommand(arguments);
                case APPEND_TO_FRAGMENT:
                    return processAppendToFragmentCommand(arguments);
                case REPLICATE_FRAGMENT_DATA:
                    return processReplicateFragmentCommand(arguments);
                case DELETE_FRAGMENT_DATA:
                    return processDeleteFragmentExemplar(arguments);
                case DESTROY_FRAGMENT:
                    return processDestroyFragment(arguments);
                case POPULATE_NODES:
                    return processPopulateNodes(arguments);
                case META_FRAGMENT_EXEMPLAR:
                    return processAttachMetaToFragmentExemplar(arguments);
                case META_FRAGMENT_GLOBAL:
                    return processAttachMetaToFragmentGlobally(arguments);
                default:
                    throw new UnknownRegistryCommandExceptions(EWriteCommand.toString());
            }
        }catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }

    private T processInsertTupleCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException,
            IllegalCommandArgumentException, ResponseSerializationException {
        final var thisCommand = EWriteCommand.INSERT_TUPLE;
        validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        var nodeId = getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _serializer.serialize(_writeCommandHandler.insertTuple(tupleId, nodeId));
    }

    private T processInsertTupleInBulkCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException,
            IllegalCommandArgumentException, ResponseSerializationException {
        final var thisCommand = EWriteCommand.INSERT_TUPLE_BULK;
        validateNotNullArguments(arguments, thisCommand.toString());

        var tupleIdsArg = getPositionalArgumentAsSet(arguments, thisCommand, ARG_TUPLE_IDs);
        var nodeId = getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _serializer.serialize(_writeCommandHandler.insertTuple(tupleIdsArg, nodeId));
    }

    private T processDeleteTupleCommand(final List<Object> arguments)
            throws UnknownEntityIdException, WriteOperationException, IllegalCommandArgumentException,
            ResponseSerializationException {
        final var thisCommand = EWriteCommand.DELETE_TUPLE;
        validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        return _serializer.serialize(_writeCommandHandler.deleteTuple(tupleId));
    }

    private T processFormFragmentCommand(final List<Object> arguments)
            throws ResponseSerializationException, WriteOperationException, IllegalRegistryActionException, UnknownEntityIdException, DuplicateEntityRecordException, IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.FORM_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());


        var tupleIdsArg = getPositionalArgumentAsSet(arguments, thisCommand, ARG_TUPLE_IDs);
        var nodeId = getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        var fragmentId = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _serializer.serialize(_writeCommandHandler.formFragment(tupleIdsArg, fragmentId, nodeId));
    }

    private T processAppendToFragmentCommand(final List<Object> arguments)
            throws WriteOperationException, DuplicateEntityRecordException, UnknownEntityIdException, IllegalCommandArgumentException, ResponseSerializationException {
        final var thisCommand = EWriteCommand.APPEND_TO_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        var fragmentId = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _serializer.serialize(_writeCommandHandler.appendTupleToFragment(tupleId, fragmentId));
    }

    private T processReplicateFragmentCommand(final List<Object> arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException, IllegalCommandArgumentException, ReadOperationException, ResponseSerializationException {
        final var thisCommand = EWriteCommand.REPLICATE_FRAGMENT_DATA;
        validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var nodeIdA = getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        var nodeIdB = getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID_B);
        return _serializer.serialize(_writeCommandHandler.replicateFragment(fragmentId, nodeIdA, nodeIdB));
    }

    private T processDeleteFragmentExemplar(final List<Object> arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException,
            IllegalCommandArgumentException, ReadOperationException, ResponseSerializationException {
        final var thisCommand = EWriteCommand.DELETE_FRAGMENT_DATA;
        validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var nodeId =  getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _serializer.serialize(_writeCommandHandler.deleteFragmentExemplar(fragmentId, nodeId));
    }

    private T processDestroyFragment(final List<Object> arguments)
            throws ResponseSerializationException, UnknownEntityIdException, IllegalCommandArgumentException,
            WriteOperationException, ReadOperationException {
        final var thisCommand = EWriteCommand.DESTROY_FRAGMENT;
        validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _serializer.serialize(_writeCommandHandler.deleteFragmentCompletely(fragmentId));
    }

    private T processPopulateNodes(final List<Object> arguments)
            throws IllegalRegistryActionException, ResponseSerializationException, WriteOperationException,
            IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.POPULATE_NODES;
        validateNotNullArguments(arguments, thisCommand.toString());

        var nodeIdsArg = getPositionalArgumentAsSet(arguments, thisCommand, ARG_NODE_IDs);
        return _serializer.serialize(_writeCommandHandler.populateNodes(nodeIdsArg));
    }

    private T processAttachMetaToFragmentExemplar(final List<Object> arguments)
            throws IllegalCommandArgumentException, UnknownEntityIdException, WriteOperationException,
            ResponseSerializationException {
        final var thisCommand = EWriteCommand.META_FRAGMENT_EXEMPLAR;
        validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var nodeId =  getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        var metaTag = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_TAG);
        var metaValue =  getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_VALUE);
        return _serializer.serialize(
                _writeCommandHandler.addMetaToFragmentExemplar(fragmentId, nodeId, metaTag, metaValue));
    }

    private T processAttachMetaToFragmentGlobally(final List<Object> arguments)
            throws IllegalCommandArgumentException, UnknownEntityIdException, WriteOperationException,
            ResponseSerializationException {
        final var thisCommand = EWriteCommand.META_FRAGMENT_EXEMPLAR;
        validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var metaTag = getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_TAG);
        var metaValue =  getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_VALUE);
        return _serializer.serialize(
                _writeCommandHandler.addMetaToFragmentGlobal(fragmentId, metaTag, metaValue));
    }
}
