package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import org.jetbrains.annotations.NotNull;

public class UnknownEntityIdException extends MddeRegistryException {
    /**
     * Custom message constructor
     * @param message Meaningful message
     */
    public UnknownEntityIdException(@NotNull String message){
        super(message);
    }

    /**
     * Constructor with predefined message about attempting to manipulate an entity with an Id that doesn't exist in the Registry
     * @param triedToAddDuplicate Type of the entity
     * @param duplicateId Entity unique id
     */
    public UnknownEntityIdException(RegistryEntityType triedToAddDuplicate, String duplicateId){
        super(String.format("Attempted to add a duplicate %s with id '%s'", triedToAddDuplicate.name(), duplicateId));
    }
}
