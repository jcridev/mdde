package dev.jcri.mdde.registry.store.queue;

import dev.jcri.mdde.registry.store.queue.actions.DataAction;
import dev.jcri.mdde.registry.utility.QueueThrowing;
/**
 * Queue of the operations to be performed in the backend data store
 */
public interface IDataShuffleQueue extends QueueThrowing<DataAction> {
    // We don't extend the standard Queue<> interface because we want to throw appropriate exceptions from the queue
    // methods. Such as IOException without inappropriately wrapping into RuntimeException or a similar one.
    // Implementation for this queue might be a database, a file or anything else. If you want an in-memory queue,
    // just wrap an implementation of Queue<DataAction> into this interface
}
