package dev.jcri.mdde.registry.control.command.sequential;


import dev.jcri.mdde.registry.control.CommandParserReadBase;
import dev.jcri.mdde.registry.control.ICommandParser;
import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.server.responders.ReadCommandResponder;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;
import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.shared.store.response.FullRegistry;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.exceptions.ResponseSerializationException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument.*;


public class SequentialReadCommandParser<T> extends CommandParserReadBase<T, List<Object>> {

    private final ReadCommandResponder _readCommandHandler;

    public SequentialReadCommandParser(ReadCommandResponder readCommandHandler, IResponseSerializer<T> serializer){
        super(serializer);
        Objects.requireNonNull(readCommandHandler, "Read commands handlers can't be null");
        _readCommandHandler = readCommandHandler;
    }

    protected FullRegistry processGetFullRegistryCommand() throws ReadOperationException {
        return _readCommandHandler.getFullRegistry();
    }

    protected Set<String> processFindTupleCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.FIND_TUPLE;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        return _readCommandHandler.getTupleNodes(tupleId);
    }

    protected String processFindTupleFragmentCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.FIND_TUPLE_FRAGMENT;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        return _readCommandHandler.getTupleFragment(tupleId);
    }

    protected Set<String> processFindFragmentNodesCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.FIND_FRAGMENT;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _readCommandHandler.getFragmentNodes(fragmentId);
    }

    protected Set<String> processGetFragmentTuplesCommand(List<Object> arguments)
            throws ReadOperationException, IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.GET_FRAGMENT_TUPLES;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _readCommandHandler.getFragmentTuples(fragmentId);
    }

    protected int processCountFragmentsCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.COUNT_FRAGMENT;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        return _readCommandHandler.getCountFragment(fragmentId);
    }

    protected int processCountTuplesCommand(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.COUNT_TUPLE;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var tupleId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_TUPLE_ID);
        return _readCommandHandler.getCountTuple(tupleId);
    }

    protected Set<String> processGetNodesCommand(){
        return _readCommandHandler.getNodes();
    }

    protected String processGetFragmentMetaGlobalValue(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.META_FRAGMENT_GLOBAL;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var metaTag = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_TAG);

        return _readCommandHandler.getMetaFragmentGlobal(fragmentId, metaTag);
    }

    protected String processGetFragmentMetaExemplarValue(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.META_FRAGMENT_EXEMPLAR;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var fragmentId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_ID);
        var nodeId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        var metaTag = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_FRAGMENT_META_TAG);

        return _readCommandHandler.getMetaFragmentExemplar(fragmentId, nodeId, metaTag);
    }

    protected Set<String> processGetUnassignedTuplesForNode(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.NODE_TUPLES_UNASSIGNED;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var nodeId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _readCommandHandler.getNodeUnassignedTuples( nodeId);
    }

    protected Set<String> processGetNodeFragments(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.NODE_FRAGMENTS;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var nodeId = CommandParserHelper.sharedInstance().getPositionalArgumentAsString(arguments, thisCommand, ARG_NODE_ID);
        return _readCommandHandler.getNodeFragments(nodeId);
    }

    protected FragmentCatalog processGetFragmentCatalog(List<Object> arguments)
            throws IllegalCommandArgumentException {
        final EReadCommand thisCommand = EReadCommand.GET_ALL_FRAGMENTS_NODES_WITH_META;
        CommandParserHelper.sharedInstance().validateNotNullArguments(arguments, thisCommand.toString());

        var metaTagsLocal = CommandParserHelper.sharedInstance().getPositionalArgumentAsSet(arguments, thisCommand, ARG_FRAGMENT_META_TAGS_LOCAL);
        var metaTagsGlobal = CommandParserHelper.sharedInstance().getPositionalArgumentAsSet(arguments, thisCommand, ARG_FRAGMENT_META_TAGS_GLOBAL);
        return _readCommandHandler.getFragmentCatalog(metaTagsLocal, metaTagsGlobal);
    }
}
