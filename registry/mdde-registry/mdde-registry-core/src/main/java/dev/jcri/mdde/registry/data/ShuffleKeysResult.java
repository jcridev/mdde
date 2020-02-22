package dev.jcri.mdde.registry.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Data shuffle result container.
 */
public class ShuffleKeysResult {
    private final Set<String> _processedKeys;
    private final Set<String> _failedKeys;
    private final Throwable _error;

    /**
     * Constructor
      * @param processedKeys Keys that were processed successfully
     */
    public ShuffleKeysResult(Set<String> processedKeys){
        this(processedKeys, null, null);
    }

    /**
     * Constructor
     * @param processedKeys Keys that were processed successfully
     * @param failedKeys Keys that failed to be processed
     * @param error Processing error
     */
    public ShuffleKeysResult(Set<String> processedKeys, Set<String> failedKeys, Throwable error){
        if(processedKeys != null) {
            _processedKeys = Collections.unmodifiableSet(processedKeys);
        }
        else{
            _processedKeys = Collections.unmodifiableSet(new HashSet<>());
        }
        if(failedKeys != null) {
            _failedKeys = Collections.unmodifiableSet(failedKeys);
        }
        else{
            _failedKeys =  Collections.unmodifiableSet(new HashSet<>());
        }
        _error = error;
    }

    public Set<String> getProcessedKeys() {
        return _processedKeys;
    }

    public Set<String> getFailedKeys() {
        return _failedKeys;
    }

    public Throwable getError() {
        return _error;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        // Success
        strBuilder.append("Successfully processed ").append(_processedKeys.size()).append(" keys:");
        for (Iterator<String> iterator = _processedKeys.iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            strBuilder.append(key);
            if(iterator.hasNext()){
                strBuilder.append(", ");
            }
        }
        strBuilder.append("; ");
        // Failed
        strBuilder.append("Failed to shuffle ").append(_failedKeys.size()).append(" keys:");
        for (Iterator<String> iterator = _failedKeys.iterator(); iterator.hasNext(); ) {
            String missedKey = iterator.next();
            strBuilder.append(missedKey);
            if(iterator.hasNext()){
                strBuilder.append(", ");
            }
        }
        strBuilder.append(";");
        // Error
        if(_error != null){
            strBuilder.append("Error: ");
            var errorMessage = _error.getMessage();
            strBuilder.append("'");
            if(errorMessage != null) {
                strBuilder.append(errorMessage);
            }
            else{
                strBuilder.append(_error.getClass().getCanonicalName());
            }
            strBuilder.append("'.");
        }
        return strBuilder.toString();
    }
}
