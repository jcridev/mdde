package dev.jcri.mdde.registry.server.responders;

import dev.jcri.mdde.registry.store.IReadCommandHandler;
import dev.jcri.mdde.registry.store.IWriteCommandHandler;
import dev.jcri.mdde.registry.store.exceptions.*;
import dev.jcri.mdde.registry.store.queue.IDataShuffleQueue;
import dev.jcri.mdde.registry.store.queue.actions.DataCopyAction;
import dev.jcri.mdde.registry.store.queue.actions.DataDeleteAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class WriteCommandResponder {
    private static final Logger logger = LogManager.getLogger(WriteCommandResponder.class);

    private final IWriteCommandHandler _writeHandler;
    private final IReadCommandHandler _readHandler;
    private final IDataShuffleQueue _dataShuffleQueue;

    public WriteCommandResponder(IWriteCommandHandler writeHandler,
                                 IReadCommandHandler readHandler,
                                 IDataShuffleQueue dataShuffleQueue){
        Objects.requireNonNull(writeHandler, "Write handler is not set to the WriteCommandResponder.");
        Objects.requireNonNull(readHandler, "Read handler is not set to the WriteCommandResponder.");
        Objects.requireNonNull(dataShuffleQueue, "Data shuffle queue implementation " +
                "is not set to the WriteCommandResponder.");

        _writeHandler = writeHandler;
        _readHandler = readHandler;
        _dataShuffleQueue = dataShuffleQueue;
    }

    public void insertTuple(final String tupleId, final String nodeId)
            throws WriteOperationException, UnknownEntityIdException, DuplicateEntityRecordException {
        _writeHandler.insertTuple(tupleId, nodeId);
    }

    public void insertTuple(final Set<String> tupleIds, final String nodeId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException {
        _writeHandler.insertTuple(tupleIds, nodeId);
    }

    public void deleteTuple(final String tupleId) throws UnknownEntityIdException, WriteOperationException{
        _writeHandler.deleteTuple(tupleId);
    }

    public String formFragment(final Set<String> tupleIds, final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, DuplicateEntityRecordException,
            IllegalRegistryActionException{
        return _writeHandler.formFragment(tupleIds, fragmentId, nodeId);
    }

    public void appendTupleToFragment(final String tupleId, final String fragmentId)
            throws DuplicateEntityRecordException, UnknownEntityIdException, WriteOperationException{
        _writeHandler.appendTupleToFragment(tupleId, fragmentId);
    }

    public void replicateFragment(final String fragmentId, final String sourceNodeId, final String destinationNodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException,
            ReadOperationException {
        // Get fragment tuples
        var tuples = _readHandler.getFragmentTuples(fragmentId);
        // Adjust registry
        _writeHandler.replicateFragment(fragmentId, sourceNodeId, destinationNodeId);
        // Put action to the data shuffle queue
        try {
            _dataShuffleQueue.add(new DataCopyAction(tuples, sourceNodeId, destinationNodeId));
        } catch (IOException e) {
            // Roll back the registry
            _writeHandler.deleteFragmentExemplar(fragmentId, destinationNodeId);
            // Fail the operation
            throw new WriteOperationException("Unable to put COPY action to the data shuffler queue", e);
        }
    }

    public void deleteFragmentExemplar(final String fragmentId, final String nodeId)
            throws UnknownEntityIdException, WriteOperationException, IllegalRegistryActionException,
            ReadOperationException {
        // Get fragment tuples
        var tuples = _readHandler.getFragmentTuples(fragmentId);
        // Adjust registry
        _writeHandler.deleteFragmentExemplar(fragmentId, nodeId);
        // Put action to the data shuffle queue
        try {
            _dataShuffleQueue.add(new DataDeleteAction(tuples, nodeId));
        } catch (IOException e) {
            // Roll back the registry
            var fragmentNodes = _readHandler.getFragmentNodes(fragmentId);
            if(fragmentNodes != null && fragmentNodes.size() > 0){
                _writeHandler.replicateFragment(fragmentId, fragmentNodes.iterator().next(), nodeId);
            }
            // Fail the operation
            throw new WriteOperationException("Unable to put COPY action to the data shuffler queue", e);
        }
    }

    public String deleteFragmentCompletely(final String fragmentId) throws UnknownEntityIdException,
            ReadOperationException, WriteOperationException {
        var fragmentNodes = _readHandler.getFragmentNodes(fragmentId);
        var tuples = _readHandler.getFragmentTuples(fragmentId);

        var result = _writeHandler.deleteFragmentCompletely(fragmentId);
        try {
            for(var nodeId: fragmentNodes){
                    _dataShuffleQueue.add(new DataDeleteAction(tuples, nodeId));
            }
        } catch (IOException e) {
            throw new WriteOperationException("Unable to put DELETE action to the data shuffler queue", e);
        }
        return result;
    }

    public boolean populateNodes(final Set<String> nodeIds)
            throws IllegalRegistryActionException, WriteOperationException{
        return _writeHandler.populateNodes(nodeIds);
    }

    public void addMetaToFragmentGlobal(final String fragmentId, final String metaField, final String metaValue)
            throws UnknownEntityIdException, WriteOperationException{

        _writeHandler.addMetaToFragmentGlobal(fragmentId, metaField, metaValue);
    }

    void addMetaToFragmentExemplar(final String fragmentId,
                                   final String nodeId,
                                   final String metaField,
                                   final String metaValue)
            throws UnknownEntityIdException, WriteOperationException{
        _writeHandler.addMetaToFragmentExemplar(fragmentId, nodeId, metaField, metaValue);
    }

    public void resetFragmentsMeta(){
        _writeHandler.resetFragmentsMeta();
    }

    public void reset() throws WriteOperationException{
        //TODO: Reset data store
        _writeHandler.reset();
    }
}
