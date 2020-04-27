package dev.jcri.mdde.registry.control;

import dev.jcri.mdde.registry.control.exceptions.CommandException;
import dev.jcri.mdde.registry.control.serialization.ResponseSerializerBase;
import dev.jcri.mdde.registry.shared.commands.EReadCommand;
import dev.jcri.mdde.registry.shared.store.response.FragmentCatalog;
import dev.jcri.mdde.registry.shared.store.response.FullRegistryAllocation;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

/**
 * Basic abstract router for the READ commands
 * @param <TOut> Expected result type
 * @param <TArgs> Arguments container type
 */
public abstract class CommandParserReadBase<TOut, TArgs>
        implements ICommandParser<TOut, EReadCommand, TArgs> {

    private static final Logger logger = LogManager.getLogger(CommandParserReadBase.class);
    protected final ResponseSerializerBase<TOut> _serializer;

    protected CommandParserReadBase(ResponseSerializerBase<TOut> serializer){
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
            logger.error(ex.getMessage(), ex);
            return _serializer.serializeException(ex);
        }
    }


    protected abstract FullRegistryAllocation processGetFullRegistryCommand()
            throws ReadOperationException;

    protected abstract Set<String> processFindTupleCommand(TArgs arguments)
            throws CommandException;

    protected abstract Set<String> processGetUnassignedTuplesForNode(TArgs arguments)
            throws CommandException;

    protected abstract Set<String> processGetNodeFragments(TArgs arguments)
            throws CommandException;

    protected abstract String processFindTupleFragmentCommand(TArgs arguments)
            throws CommandException;

    protected abstract Set<String> processFindFragmentNodesCommand(TArgs arguments)
            throws CommandException;

    protected abstract Set<String> processGetFragmentTuplesCommand(TArgs arguments)
            throws ReadOperationException, CommandException;

    protected abstract FragmentCatalog processGetFragmentCatalog(TArgs arguments)
            throws CommandException;

    protected abstract int processCountFragmentsCommand(TArgs arguments)
            throws CommandException;

    protected abstract int processCountTuplesCommand(TArgs arguments)
            throws CommandException;

    protected abstract Set<String> processGetNodesCommand();

    protected abstract String processGetFragmentMetaExemplarValue(TArgs arguments)
            throws CommandException;

    protected abstract String processGetFragmentMetaGlobalValue(TArgs arguments)
            throws CommandException;
}
