package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.CommandParserWriteBase;
import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.server.responders.WriteCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.store.exceptions.*;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;

import java.util.List;
import java.util.Objects;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;


public class SequentialWriteCommandParser<T> extends CommandParserWriteBase<T, List<Object>> {
    private final WriteCommandResponder _writeCommandHandler;
    public SequentialWriteCommandParser(WriteCommandResponder writeCommandHandler, IResponseSerializer<T> serializer) {
        super(serializer);
        Objects.requireNonNull(writeCommandHandler, "Write commands handlers can't be null");
        _writeCommandHandler = writeCommandHandler;
    }

    protected boolean processInsertTupleCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException,
            IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.INSERT_TUPLE;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        var nodeId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _writeCommandHandler.insertTuple(tupleId, nodeId);
    }

    protected boolean processInsertTupleInBulkCommand(final List<Object> arguments)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException,
            IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.INSERT_TUPLE_BULK;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var tupleIdsArg = CommandParserHelper.sharedInstance().getPositionalArgumentAsSet(arguments, thisCommand, ARG_TUPLE_IDs);
        var nodeId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _writeCommandHandler.insertTuple(tupleIdsArg, nodeId);
    }

    protected boolean processDeleteTupleCommand(final List<Object> arguments)
            throws UnknownEntityIdException, WriteOperationException, IllegalCommandArgumentException    {
        final var thisCommand = EWriteCommand.DELETE_TUPLE;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        return _writeCommandHandler.deleteTuple(tupleId);
    }

    protected boolean processFormFragmentCommand(final List<Object> arguments)
            throws WriteOperationException, IllegalRegistryActionException, UnknownEntityIdException, DuplicateEntityRecordException, IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.FORM_FRAGMENT;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());


        var tupleIdsArg = CommandParserHelper.sharedInstance().getPositionalArgumentAsSet(arguments, thisCommand, ARG_TUPLE_IDs);
        var nodeId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _writeCommandHandler.formFragment(tupleIdsArg, fragmentId, nodeId);
    }

    protected boolean processAppendToFragmentCommand(final List<Object> arguments)
            throws WriteOperationException, DuplicateEntityRecordException, UnknownEntityIdException, IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.APPEND_TO_FRAGMENT;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _writeCommandHandler.appendTupleToFragment(tupleId, fragmentId);
    }

    protected boolean processReplicateFragmentCommand(final List<Object> arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException, IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.REPLICATE_FRAGMENT_DATA;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var nodeIdA = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        var nodeIdB = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID_B);
        try {
            return _writeCommandHandler.replicateFragment(fragmentId, nodeIdA, nodeIdB);
        } catch (ReadOperationException e) {
            throw new WriteOperationException(e);
        }
    }

    protected boolean processDeleteFragmentExemplar(final List<Object> arguments)
            throws WriteOperationException, UnknownEntityIdException, IllegalRegistryActionException,
            IllegalCommandArgumentException, ReadOperationException {
        final var thisCommand = EWriteCommand.DELETE_FRAGMENT_DATA;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var nodeId =  CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _writeCommandHandler.deleteFragmentExemplar(fragmentId, nodeId);
    }

    protected String processDestroyFragment(final List<Object> arguments)
            throws UnknownEntityIdException, IllegalCommandArgumentException,
            WriteOperationException, ReadOperationException {
        final var thisCommand = EWriteCommand.DESTROY_FRAGMENT;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _writeCommandHandler.deleteFragmentCompletely(fragmentId);
    }

    protected boolean processPopulateNodes(final List<Object> arguments)
            throws IllegalRegistryActionException,  WriteOperationException, IllegalCommandArgumentException {
        final var thisCommand = EWriteCommand.POPULATE_NODES;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var nodeIdsArg = CommandParserHelper.sharedInstance().getPositionalArgumentAsSet(arguments, thisCommand, ARG_NODE_IDs);
        return _writeCommandHandler.populateNodes(nodeIdsArg);
    }

    protected boolean processAttachMetaToFragmentExemplar(final List<Object> arguments)
            throws IllegalCommandArgumentException, UnknownEntityIdException, WriteOperationException
            {
        final var thisCommand = EWriteCommand.META_FRAGMENT_EXEMPLAR;
                CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var nodeId =  CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        var metaTag = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_TAG);
        var metaValue =  CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_VALUE);
        return _writeCommandHandler.addMetaToFragmentExemplar(fragmentId, nodeId, metaTag, metaValue);
    }

    protected boolean processAttachMetaToFragmentGlobally(final List<Object> arguments)
            throws IllegalCommandArgumentException, UnknownEntityIdException, WriteOperationException
            {
        final var thisCommand = EWriteCommand.META_FRAGMENT_GLOBAL;
                CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var metaTag = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_TAG);
        var metaValue =  CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_VALUE);
        return _writeCommandHandler.addMetaToFragmentGlobal(fragmentId, metaTag, metaValue);
    }
}
