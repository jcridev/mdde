package dev.jcri.mdde.registry.server.tcp;

import java.util.concurrent.locks.ReentrantLock;

public final class CancellationToken {
    private ReentrantLock _lockCancellation = new ReentrantLock();
    private boolean _isCancelled = false;

    public boolean getCancelled(){
        return _isCancelled;
    }

    public void cancel(){
        _lockCancellation.lock();
        try{
            _isCancelled =true;
        }
        finally {
            _lockCancellation.unlock();
        }
    }

    @Override
    public String toString() {
        return String.format("Cancellation is requested: %b", _isCancelled);
    }
}
