package dev.jcri.mdde.registry.control.command.sequential;

import dev.jcri.mdde.registry.control.exceptions.IllegalCommandArgumentException;
import dev.jcri.mdde.registry.shared.commands.ExpectedCommandArgument;
import dev.jcri.mdde.registry.shared.commands.ICommand;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Common methods for working with the ICommands definitions
 */
public class CommandParserHelper {

    private CommandParserHelper() {}

    private static class LazyHolder {
        static final CommandParserHelper INSTANCE = new CommandParserHelper();
    }

    public static CommandParserHelper sharedInstance() {
        return LazyHolder.INSTANCE;
    }

    public void validateNotNullArguments(final List<Object> arguments, final String commandTitle){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments", commandTitle));
    }

    /**
     * Generate error message for IllegalCommandArgumentPosition
     * @param command Command object
     * @param argument Out of place argument
     * @return Text describing the out of place error
     */
    public String getPositionalArgumentError(final ICommand command,
                                             final ExpectedCommandArgument argument){
        return getPositionalArgumentError(command, argument, command.getExpectedArguments().indexOf(argument));
    }

    /**
     * Generate error message for IllegalCommandArgumentPosition
     * @param command Command object
     * @param argument Out of place argument
     * @param position Real position in the sequence
     * @return Text describing the out of place error
     */
    public String getPositionalArgumentError(final ICommand command,
                                             final ExpectedCommandArgument argument,
                                             final int position){
        return String.format("%s must be invoked with %s at position %d",
                command.getCommand(), argument.toString(), position);
    }

    /**
     * Generate an error message for IllegalCommandArgument
     * @param command Command
     * @param argument Argument
     * @return Text describing illegal argument
     */
    private String getIllegalArgumentError(ICommand command,
                                           ExpectedCommandArgument argument){
        return String.format("%s invoked with an illegal argument %s", command.getCommand(), argument.toString());
    }

    /**
     * Read argument as a String value from the position as specified in the corresponding ICommands
     * @param arguments List of arguments
     * @param command Command name
     * @param argument Argument description object
     * @return Argument value
     */
    public String getPositionalArgumentAsString(List<Object> arguments,
                                                ICommand command,
                                                ExpectedCommandArgument argument)
            throws IllegalCommandArgumentException {
        var argIndex = command.getExpectedArguments().indexOf(argument);
        if(argIndex < 0){
            throw new IllegalCommandArgumentException(getIllegalArgumentError(command, argument));
        }
        return (String) Objects.requireNonNull(arguments.get(argIndex),
                                                getPositionalArgumentError(command, argument, argIndex));
    }

    /**
     * Read argument as a Boolean value from the position as specified in the corresponding ICommands
     * @param arguments List of arguments
     * @param command Command name
     * @param argument Argument description object
     * @return Argument value
     * @throws IllegalCommandArgumentException
     */
    public Boolean getPositionalArgumentAsBoolean(List<Object> arguments,
                                                  ICommand command,
                                                  ExpectedCommandArgument argument)
            throws IllegalCommandArgumentException {
        var argIndex = command.getExpectedArguments().indexOf(argument);
        if(argIndex < 0){
            throw new IllegalCommandArgumentException(getIllegalArgumentError(command, argument));
        }
        return (Boolean) Objects.requireNonNull(arguments.get(argIndex),
                                                getPositionalArgumentError(command, argument, argIndex));
    }

    /**
     * Read argument as a Set<String> value from the position as specified in the corresponding ICommands
     * @param arguments List of arguments
     * @param command Command name
     * @param argument Argument description object
     * @return Argument value
     */
    public Set<String> getPositionalArgumentAsSet(List<Object> arguments,
                                                  ICommand command,
                                                  ExpectedCommandArgument argument)
            throws IllegalCommandArgumentException {
        if(argument.getArgumentType() != ExpectedCommandArgument.ArgumentType.SET_STRINGS){
            throw new IllegalArgumentException(String.format("Argument %s is not a set of strings", argument.toString()));
        }

        var argIndex = command.getExpectedArguments().indexOf(argument);
        if(argIndex < 0){
            throw new IllegalCommandArgumentException(getIllegalArgumentError(command, argument));
        }
        var tupleIdsArg = Objects.requireNonNull(arguments.get(argIndex),
                getPositionalArgumentError(command, argument, argIndex));
                if(!(tupleIdsArg instanceof Set<?>)){
                    throw new IllegalArgumentException(getPositionalArgumentError(command, argument, argIndex));
                }
        return (Set<String>) tupleIdsArg;
    }
}
