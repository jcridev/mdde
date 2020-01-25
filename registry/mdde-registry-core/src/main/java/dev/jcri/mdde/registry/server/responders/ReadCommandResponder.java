package dev.jcri.mdde.registry.server.responders;

import dev.jcri.mdde.registry.shared.store.response.FullRegistry;
import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

public class ReadCommandResponder {
    private static final Logger logger = LogManager.getLogger(ReadCommandResponder.class);
    private final  IReadCommandHandler _readHandler;

    public ReadCommandResponder(IReadCommandHandler readHandler){
        Objects.requireNonNull(readHandler, "Read handler is not set to the ReadCommandResponder.");
        _readHandler = readHandler;
    }

    public FullRegistry getFullRegistry() throws ReadOperationException{
        return _readHandler.getFullRegistry();
    }

    public Set<String> getTupleNodes(final String tupleId){
        return _readHandler.getTupleNodes(tupleId);
    }

    public String getTupleFragment(final String tupleId){
        return _readHandler.getTupleFragment(tupleId);
    }

    public Set<String> getFragmentNodes(final String fragmentId){
        return _readHandler.getFragmentNodes(fragmentId);
    }

    public Set<String> getFragmentTuples(final String fragmentId) throws ReadOperationException{
        return _readHandler.getFragmentTuples(fragmentId);
    }

    public int getCountFragment(final String fragmentId){
        return _readHandler.getCountFragment(fragmentId);
    }

    public int getCountTuple(final String tupleId){
        return _readHandler.getCountTuple(tupleId);
    }

    public Set<String> getNodes(){
        return _readHandler.getNodes();
    }
}
