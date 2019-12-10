package dev.jcri.mdde.registry.store.exceptions;

import org.jetbrains.annotations.NotNull;

public class MandatoryCommandArgumentMissingException extends Exception {
    public MandatoryCommandArgumentMissingException(@NotNull String missingParamName){
        super(String.format("Mandatory parameter is missing: '%s'", missingParamName));
    }
}
