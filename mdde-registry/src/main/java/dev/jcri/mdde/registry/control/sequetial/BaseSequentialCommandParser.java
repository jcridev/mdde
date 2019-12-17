package dev.jcri.mdde.registry.control.sequetial;

import dev.jcri.mdde.registry.control.ExpectedCommandArgument;

import java.util.List;
import java.util.Objects;

public abstract class BaseSequentialCommandParser {
    protected void validateNotNullArguments(final List<Object> arguments, final String commandTitle){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments", commandTitle));
    }

    protected String getPositionalArgumentError(final String commandTitle, final ExpectedCommandArgument argument, final int position){
        return String.format("%s must be invoked with %s at position %d", commandTitle, argument.toString(), position);
    }
}
