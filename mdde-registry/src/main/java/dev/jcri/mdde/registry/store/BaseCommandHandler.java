package dev.jcri.mdde.registry.store;

public abstract class BaseCommandHandler {

    public static class ExpectedCommandArgument{
        private final String _title;
        private final ArgumentType _argumentType;

        public ExpectedCommandArgument(String title, ArgumentType argumentType) {
            this._title = title;
            this._argumentType = argumentType;
        }

        public enum ArgumentType{
            string,
            set_strings,
            list_string
        }

        @Override
        public String toString() {
            return _title;
        }

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
