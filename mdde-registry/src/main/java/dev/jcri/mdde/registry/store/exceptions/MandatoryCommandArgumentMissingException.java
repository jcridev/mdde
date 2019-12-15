package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;
import org.jetbrains.annotations.NotNull;

public class MandatoryCommandArgumentMissingException extends MddeRegistryException {
    public MandatoryCommandArgumentMissingException(@NotNull String missingParamName){
        super(String.format("Mandatory parameter is missing: '%s'", missingParamName));
    }
}
