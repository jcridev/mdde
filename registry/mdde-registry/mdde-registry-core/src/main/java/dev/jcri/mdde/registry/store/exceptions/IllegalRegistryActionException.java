package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

/**
 * Thrown when there is an attempt to perform an action that is explicitly prohibited by the registry
 */
public class IllegalRegistryActionException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.ILLEGAL_REGISTRY_ACTION;

    private final IllegalActions _action;

    public IllegalRegistryActionException(String message, IllegalActions action){
        super(_exCode, message);
        this._action = action;
    }

    /**
     * Specific illegal action attempted
     * @return IllegalActions
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
        FormingFragmentFromNonColocatedTuples,
        LocalFragmentReplication,
        DuplicateFragmentReplication
    }
}
