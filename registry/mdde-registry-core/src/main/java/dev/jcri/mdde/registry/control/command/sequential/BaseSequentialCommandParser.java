package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument;
import dev.jcri.mdde.registry.shared.commands.ICommand;

import java.util.List;
import java.util.Objects;
import java.util.Set;


public abstract class BaseSequentialCommandParser {
    protected void validateNotNullArguments(final List<Object> arguments, final String commandTitle){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments", commandTitle));
    }

    /**
     * Generate error message for IllegalCommandArgumentPosition
     * @param commandTitle
     * @param argument
     * @param position
     * @return
     */
    protected String getPositionalArgumentError(final String commandTitle, final ExpectedCommandArgument argument, final int position){
        return String.format("%s must be invoked with %s at position %d", commandTitle, argument.toString(), position);
    }

    /**
     * Generate an error message for IllegalCommandArgument
     * @param command
     * @param argument
     * @return
     */
    private String getIllegalArgumentError(ICommand command, ExpectedCommandArgument argument){
        return String.format("%s invoked with an illegal argument %s", command.getCommand(), argument.toString());
    }

    /**
     * Read argument as a String value from the position as specified in the corresponding ICommands
     * @param arguments List of arguments
     * @param command Command name
     * @param argument Argument description object
     * @return Argument value
     */
    protected String getPositionalArgumentAsString(List<Object> arguments, ICommand command, ExpectedCommandArgument argument)
            throws IllegalCommandArgumentException {
        var argIndex = command.getExpectedArguments().indexOf(argument);
        if(argIndex < 0){
            throw new IllegalCommandArgumentException(getIllegalArgumentError(command, argument));
        }
        return (String) Objects.requireNonNull(arguments.get(argIndex),
                                                getPositionalArgumentError(command.toString(),
                                                        argument, argIndex));
    }

    /**
     * Read argument as a Boolean value from the position as specified in the corresponding ICommands
     * @param arguments List of arguments
     * @param command Command name
     * @param argument Argument description object
     * @return Argument value
     * @throws IllegalCommandArgumentException
     */
    protected Boolean getPositionalArgumentAsBoolean(List<Object> arguments, ICommand command, ExpectedCommandArgument argument)
            throws IllegalCommandArgumentException {
        var argIndex = command.getExpectedArguments().indexOf(argument);
        if(argIndex < 0){
            throw new IllegalCommandArgumentException(getIllegalArgumentError(command, argument));
        }
        return (Boolean) Objects.requireNonNull(arguments.get(argIndex),
                                                getPositionalArgumentError(command.toString(),
                                                        argument, argIndex));
    }

    /**
     * Read argument as a Set<String> value from the position as specified in the corresponding ICommands
     * @param arguments List of arguments
     * @param command Command name
     * @param argument Argument description object
     * @return Argument value
     */
    protected Set<String> getPositionalArgumentAsSet(List<Object> arguments, ICommand command, ExpectedCommandArgument argument)
            throws IllegalCommandArgumentException {
        if(argument.getArgumentType() != ExpectedCommandArgument.ArgumentType.SET_STRINGS){
            throw new IllegalArgumentException(String.format("Argument %s is not a set of strings", argument.toString()));
        }

        var argIndex = command.getExpectedArguments().indexOf(argument);
        if(argIndex < 0){
            throw new IllegalCommandArgumentException(getIllegalArgumentError(command, argument));
        }
        var tupleIdsArg = Objects.requireNonNull(arguments.get(argIndex),
                getPositionalArgumentError(command.toString(), argument, argIndex));
                if(!(tupleIdsArg instanceof Set<?>)){
                    throw new IllegalArgumentException(getPositionalArgumentError(command.toString(), argument, argIndex));
                }
        return (Set<String>) tupleIdsArg;
    }
}
