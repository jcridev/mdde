package dev.jcri.mdde.registry.store.exceptions;

import dev.jcri.mdde.registry.exceptions.EErrorCode;
import dev.jcri.mdde.registry.exceptions.MddeRegistryException;

public class MandatoryCommandArgumentMissingException extends MddeRegistryException {
    private final static EErrorCode _exCode = EErrorCode.MISSING_COMMAND_ARGUMENT;

    public MandatoryCommandArgumentMissingException(String missingParamName){
        super(_exCode, String.format("Mandatory parameter is missing: '%s'", missingParamName));
    }
}
