package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Thrown when there is an attempt to perform an action that is explicitly prohibited by the registry
 */
public class IllegalRegistryActionException extends MddeRegistryException {
    private final IllegalActions _action;

    public IllegalRegistryActionException(String message, IllegalActions action){
        super(message);
        this._action = action;
    }

    /**
     * Specific illegal action attempted
     * @return
     */
    public IllegalActions getAction() {
        return _action;
    }

    /**
     * List of possible illegal actions
     */
    public enum IllegalActions {
        UniqueFragmentRemoval,
        AttemptToSeedNonEmptyRegistry,
        FormingFragmentFromNonColocatedTuples
    }
}
