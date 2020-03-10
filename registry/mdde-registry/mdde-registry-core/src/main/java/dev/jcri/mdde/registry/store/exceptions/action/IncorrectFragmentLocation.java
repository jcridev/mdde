package dev.jcri.mdde.registry.store.exceptions.action;

import dev.jcri.mdde.registry.exceptions.EErrorCode;

/**
 * Command specifies incorrect current location of the fragment
 */
public class IncorrectFragmentLocation extends IllegalRegistryActionException{
    private final static EErrorCode _exCode = EErrorCode.INCORRECT_FRAGMENT_LOCATION;

    public IncorrectFragmentLocation(String message) {
        this(message, null);
    }

    public IncorrectFragmentLocation(Throwable cause) {
        this(null, cause);
    }

    public IncorrectFragmentLocation(String message, Throwable cause) {
        super(_exCode, message, cause);
    }
}
