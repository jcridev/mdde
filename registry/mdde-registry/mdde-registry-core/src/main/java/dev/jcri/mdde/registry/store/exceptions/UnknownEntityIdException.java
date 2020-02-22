package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class UnknownEntityIdException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.UNKNOWN_ENTITY_ID;

    /**
     * Custom message constructor
     * @param message Meaningful message
     */
    protected UnknownEntityIdException(String message){
        super(_exCode, message);
    }

    /**
     * Constructor with predefined message about attempting to manipulate an entity with an Id that doesn't exist in the Registry
     * @param triedToAddDuplicate Type of the entity
     * @param duplicateId Entity unique id
     */
    public UnknownEntityIdException(RegistryEntityType triedToAddDuplicate, String duplicateId){
        this(String.format("Attempted to add a duplicate %s with id '%s'", triedToAddDuplicate.name(), duplicateId));
    }
}
