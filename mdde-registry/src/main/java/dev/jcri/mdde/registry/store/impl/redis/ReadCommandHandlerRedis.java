package dev.jcri.mdde.registry.store.impl.redis;

import dev.jcri.mdde.registry.store.ReadCommandHandler;
import dev.jcri.mdde.registry.store.response.FullRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReadCommandHandlerRedis extends ReadCommandHandler {
    @Override
    public FullRegistry getFullRegistry() {
        return null;
    }

    @Override
    public String getTupleNodes(@NotNull String tupleId) {
        return null;
    }

    @Override
    public String getTupleFragment(@NotNull String tupleId) {
        return null;
    }

    @Override
    public String getFragmentNodes(@NotNull String fragmentId) {
        return null;
    }

    @Override
    public String getFragmentTuples(@NotNull String fragmentId) {
        return null;
    }

    @Override
    public int getCountFragment(@NotNull String fragmentId) {
        return 0;
    }

    @Override
    public int getCountTuple(@NotNull String tupleId) {
        return 0;
    }

    @Override
    public List<String> getNodes() {
        return null;
    }

    @Override
    public boolean getIsNodeExists(@NotNull String nodeId) {
        return false;
    }
}
