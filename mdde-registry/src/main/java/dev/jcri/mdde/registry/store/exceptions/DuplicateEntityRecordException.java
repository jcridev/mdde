package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import org.jetbrains.annotations.NotNull;

/**
 * Error thrown when a duplicate entity is encountered in the registry where it shouldn't
 */
public class DuplicateEntityRecordException extends MddeRegistryException {
    /**
     * Custom message constructor
     * @param message Meaningful message
     */
    public DuplicateEntityRecordException(@NotNull String message){
        super(message);
    }

    /**
     * Constructor with predefined message about attempting to a duplicate entity id to the registry
     * @param triedToAddDuplicate Type of the duplicate entity
     * @param duplicateId Entity unique id
     */
    public DuplicateEntityRecordException(RegistryEntityType triedToAddDuplicate, String duplicateId){
        super(String.format("Attempted to add a duplicate %s with id '%s'", triedToAddDuplicate.name(), duplicateId));
    }
}
