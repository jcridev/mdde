package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class MandatoryCommandArgumentMissingException extends MddeRegistryException {
    public MandatoryCommandArgumentMissingException(String missingParamName){
        super(String.format("Mandatory parameter is missing: '%s'", missingParamName));
    }
}
