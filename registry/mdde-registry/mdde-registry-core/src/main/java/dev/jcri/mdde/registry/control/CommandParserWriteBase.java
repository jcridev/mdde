package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerBase;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.store.exceptions.*;

import java.util.Objects;

/**
 * Basic abstract router for the WRITE commands
 * @param <TOut> Expected result type
 * @param <TArgs> Arguments container type
 */
public abstract class CommandParserWriteBase<TOut, TArgs>
        implements ICommandParser<TOut, EWriteCommand, TArgs> {

    protected final ResponseSerializerBase<TOut> _serializer;

    protected CommandParserWriteBase(ResponseSerializerBase<TOut> serializer){
        Objects.requireNonNull(serializer, "Serializer can't be null");
        this._serializer = serializer;
    }

    public final TOut runCommand(EWriteCommand command, TArgs arguments){
        try {
            switch (command) {
                case INSERT_TUPLE:
                    return _serializer.serialize(processInsertTupleCommand(arguments));
                case INSERT_TUPLE_BULK:
                    return _serializer.serialize(processInsertTupleInBulkCommand(arguments));
                case DELETE_TUPLE:
                    return _serializer.serialize(processDeleteTupleCommand(arguments));
                case FORM_FRAGMENT:
                    return _serializer.serialize(processFormFragmentCommand(arguments));
                case APPEND_TO_FRAGMENT:
                    return _serializer.serialize(processAppendToFragmentCommand(arguments));
                case REPLICATE_FRAGMENT_DATA:
                    return _serializer.serialize(processReplicateFragmentCommand(arguments));
                case DELETE_FRAGMENT_DATA:
                    return _serializer.serialize(processDeleteFragmentExemplar(arguments));
                case DESTROY_FRAGMENT:
                    return _serializer.serialize(processDestroyFragment(arguments));
                case POPULATE_NODES:
                    return _serializer.serialize(processPopulateNodes(arguments));
                case META_FRAGMENT_EXEMPLAR:
                    return _serializer.serialize(processAttachMetaToFragmentExemplar(arguments));
                case META_FRAGMENT_GLOBAL:
                    return _serializer.serialize(processAttachMetaToFragmentGlobally(arguments));
                default:
                    throw new UnknownRegistryCommandExceptions(command.toString());
            }
        }
        catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }

    protected abstract boolean processInsertTupleCommand(TArgs arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException, IllegalCommandArgumentException;

    protected abstract boolean processInsertTupleInBulkCommand(TArgs arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException, IllegalCommandArgumentException;

    protected abstract boolean processDeleteTupleCommand(TArgs arguments)
            throws UnknownEntityIdException, WriteOperationException, IllegalCommandArgumentException;

    protected abstract boolean processFormFragmentCommand(TArgs arguments)
            throws WriteOperationException, IllegalRegistryActionException, UnknownEntityIdException, DuplicateEntityRecordException, IllegalCommandArgumentException;

    protected abstract boolean processAppendToFragmentCommand(TArgs arguments)
            throws WriteOperationException, DuplicateEntityRecordException, UnknownEntityIdException, IllegalCommandArgumentException;

    protected abstract boolean processReplicateFragmentCommand(TArgs arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException, IllegalCommandArgumentException;

    protected abstract boolean processDeleteFragmentExemplar(TArgs arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException, IllegalCommandArgumentException, ReadOperationException;

    protected abstract String processDestroyFragment(TArgs arguments)
            throws UnknownEntityIdException, IllegalCommandArgumentException, WriteOperationException, ReadOperationException;

    protected abstract boolean processPopulateNodes(TArgs arguments)
            throws IllegalRegistryActionException, WriteOperationException, IllegalCommandArgumentException;

    protected abstract boolean processAttachMetaToFragmentExemplar(TArgs arguments)
            throws IllegalCommandArgumentException, UnknownEntityIdException, WriteOperationException;

    protected abstract boolean processAttachMetaToFragmentGlobally(TArgs arguments)
            throws IllegalCommandArgumentException, UnknownEntityIdException, WriteOperationException;
}
