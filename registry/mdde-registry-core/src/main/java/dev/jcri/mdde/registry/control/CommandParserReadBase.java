package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.control.serialization.IResponseSerializer;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;
import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.shared.store.response.FullRegistry;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.Objects;
import java.util.Set;

public abstract class CommandParserReadBase<TOut, TArgs>
        implements ICommandParser<TOut, EReadCommand, TArgs> {

    protected final IResponseSerializer<TOut> _serializer;

    protected CommandParserReadBase(IResponseSerializer<TOut> serializer){
        Objects.requireNonNull(serializer, "Serializer can't be null");
        this._serializer = serializer;
    }

    public final TOut runCommand(EReadCommand command, TArgs arguments){
        try {
            switch (command) {
                case GET_REGISTRY:
                    return _serializer.serialize(processGetFullRegistryCommand());
                case FIND_TUPLE:
                    return _serializer.serialize(processFindTupleCommand(arguments));
                case NODE_TUPLES_UNASSIGNED:
                    return _serializer.serialize(processGetUnassignedTuplesForNode(arguments));
                case NODE_FRAGMENTS:
                    return _serializer.serialize(processGetNodeFragments(arguments));
                case FIND_TUPLE_FRAGMENT:
                    return _serializer.serialize(processFindTupleFragmentCommand(arguments));
                case FIND_FRAGMENT:
                    return _serializer.serialize(processFindFragmentNodesCommand(arguments));
                case GET_FRAGMENT_TUPLES:
                    return _serializer.serialize(processGetFragmentTuplesCommand(arguments));
                case GET_ALL_FRAGMENTS_NODES_WITH_META:
                    return _serializer.serialize(processGetFragmentCatalog(arguments));
                case COUNT_FRAGMENT:
                    return _serializer.serialize(processCountFragmentsCommand(arguments));
                case COUNT_TUPLE:
                    return _serializer.serialize(processCountTuplesCommand(arguments));
                case GET_NODES:
                    return _serializer.serialize(processGetNodesCommand());
                case META_FRAGMENT_EXEMPLAR:
                    return _serializer.serialize(processGetFragmentMetaExemplarValue(arguments));
                case META_FRAGMENT_GLOBAL:
                    return _serializer.serialize(processGetFragmentMetaGlobalValue(arguments));
                default:
                    throw new UnknownRegistryCommandExceptions(command.toString());
            }
        }
        catch (Exception ex){
            return _serializer.serializeException(ex);
        }
    }


    protected abstract FullRegistry processGetFullRegistryCommand()
            throws ReadOperationException;

    protected abstract Set<String> processFindTupleCommand(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract Set<String> processGetUnassignedTuplesForNode(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract Set<String> processGetNodeFragments(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract String processFindTupleFragmentCommand(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract Set<String> processFindFragmentNodesCommand(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract Set<String> processGetFragmentTuplesCommand(TArgs arguments)
            throws ReadOperationException, IllegalCommandArgumentException;

    protected abstract FragmentCatalog processGetFragmentCatalog(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract int processCountFragmentsCommand(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract int processCountTuplesCommand(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract Set<String> processGetNodesCommand();

    protected abstract String processGetFragmentMetaExemplarValue(TArgs arguments)
            throws IllegalCommandArgumentException;

    protected abstract String processGetFragmentMetaGlobalValue(TArgs arguments)
            throws IllegalCommandArgumentException;
}
