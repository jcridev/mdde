package dev.jcri.mdde.registry.server.tcp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Handler for the **read** queries
 */
public class ReadCommandHandler {
    /**
     * Run a selected command
     * @param command Read command tag
     * @param arguments Deserialized arguments
     * @return Result as JSON
     */
    public String runCommand(Commands command, Map<String, String> arguments){
        // TODO

        return null;
    }


    public enum Commands {
        GET_REGISTRY("GETALL"),
        FIND_TUPLE("FINDTUPLE"),
        FIND_TUPLE_FRAGMENT("TUPLEFRAGMENT"),
        FIND_FRAGMENT("FINDFRAGMENT"),
        GET_FRAGMENT_TUPLES("GETFRAGTUPLES"),
        COUNT_FRAGMENT("COUNTFRAGMENT"),
        COUNT_TUPLE("COUNTTUPLE");

        private final String _command;

        /**
         * @param command
         */
        Commands(final String command) {
            this._command = command;
        }

        private static List<Commands> _commandsRegistry = Arrays.asList(Commands.values());

        @Override
        public String toString() {
            return _command;
        }

        public static Commands getCommandTag(String tagparam){
            if(tagparam == null || tagparam.isEmpty()){
                throw new IllegalArgumentException("tag can't be null or empty");
            }

            for(Commands ct: _commandsRegistry){
                if(ct._command.equals(tagparam)) {
                    return ct;
                }
            }

            return null;
        }
    }
}
