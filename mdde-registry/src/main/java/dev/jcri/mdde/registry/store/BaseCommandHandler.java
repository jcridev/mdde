package dev.jcri.mdde.registry.store;

import java.util.List;
import java.util.Objects;

public abstract class BaseCommandHandler {

    protected void validateNotNullArguments(final List<Object> arguments, final String commandTitle){
        Objects.requireNonNull(arguments, String.format("%s can't be invoked without arguments", commandTitle));
    }

    protected String getPositionalArgumentError(final String commandTitle, final ExpectedCommandArgument argument, final int position){
        return String.format("%s must be invoked with %s at position %d", commandTitle, argument.toString(), position);
    }

    /**
     * Class containing the description of an argument expected by a command
     */
    public static class ExpectedCommandArgument{
        private final String _title;
        private final ArgumentType _argumentType;

        /**
         * Constructor
         * @param title Argument title, should be unique
         * @param argumentType Argument type
         */
        public ExpectedCommandArgument(String title, ArgumentType argumentType) {
            if(title == null || title.isEmpty()){
                throw new IllegalArgumentException("Argument title can't be null or empty");
            }

            this._title = title;
            this._argumentType = argumentType;
        }

        /**
         * Types of the arguments supported
         */
        public enum ArgumentType{
            string,
            set_strings
        }

        /**
         * Returns argument title
         * @return
         */
        @Override
        public String toString() {
            return _title;
        }

        /**
         * Expected type of the argument
         * @return
         */
        public ArgumentType getArgumentType(){
            return _argumentType;
        }
    }

    public static final ExpectedCommandArgument ARG_TUPLE_ID = new ExpectedCommandArgument("Tuple ID", ExpectedCommandArgument.ArgumentType.string);
    public static final ExpectedCommandArgument ARG_TUPLE_IDs = new ExpectedCommandArgument("Tuple IDs", ExpectedCommandArgument.ArgumentType.set_strings);
    public static final ExpectedCommandArgument ARG_NODE_ID = new ExpectedCommandArgument("Node ID", ExpectedCommandArgument.ArgumentType.string);
    public static final ExpectedCommandArgument ARG_NODE_IDs = new ExpectedCommandArgument("Node IDs", ExpectedCommandArgument.ArgumentType.set_strings);;
    public static final ExpectedCommandArgument ARG_NODE_ID_B = new ExpectedCommandArgument("2nd Node ID", ExpectedCommandArgument.ArgumentType.string);
    public static final ExpectedCommandArgument ARG_FRAGMENT_ID = new ExpectedCommandArgument("Fragment ID", ExpectedCommandArgument.ArgumentType.string);
}
