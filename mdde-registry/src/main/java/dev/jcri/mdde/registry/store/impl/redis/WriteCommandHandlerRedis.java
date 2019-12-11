package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.ReadCommandHandler;
import dev.jcri.mdde.registry.store.WriteCommandHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WriteCommandHandlerRedis extends WriteCommandHandler {
    public WriteCommandHandlerRedis(ReadCommandHandler readCommandHandler) {
        super(readCommandHandler);
    }

    @Override
    protected String runInsertTuple(@NotNull String tupleId, @NotNull String nodeId, String fragmentId) {
        return null;
    }

    @Override
    protected String runInsertTuple(@NotNull List<String> tupleId, @NotNull String nodeId, String fragmentId) {
        return null;
    }

    @Override
    protected String runDeleteTuple(@NotNull String tupleId) {
        return null;
    }

    @Override
    protected String runFormFragment(@NotNull List<String> tupleId, String fragmentId) {
        return null;
    }

    @Override
    protected String runAppendTupleToFragment(@NotNull String tupleId, @NotNull String fragmentId) {
        return null;
    }

    @Override
    protected String runReplicateFragment(@NotNull String fragmentId, @NotNull String sourceNodeId, @NotNull String destinationNodeId) {
        return null;
    }

    @Override
    protected String runDeleteFragmentExemplar(@NotNull String fragmentId, @NotNull String nodeId) {
        return null;
    }

    @Override
    protected String runCompleteFragmentDeletion(@NotNull String fragmentId) {
        return null;
    }
}
