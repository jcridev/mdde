package dev.jcri.mdde.registry.server.tcp;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the *write* operations, executes sequentially
 */
public class WriteCommandHandler {
    public enum Commands {
        GET_REGISTRY("GETALL"),
        FIND_TUPLE("FINDTUPLE");

        private final String _command;

        /**
         * @param command
         */
        Commands(final String command) {
            this._command = command;
        }

        private static List<WriteCommandHandler.Commands> _writeCommandRegistry = Arrays.asList(WriteCommandHandler.Commands.values());

        @Override
        public String toString() {
            return _command;
        }

        public static WriteCommandHandler.Commands getCommandTag(String tagparam){
            if(tagparam == null || tagparam.isEmpty()){
                throw new IllegalArgumentException("tag can't be null or empty");
            }

            for(WriteCommandHandler.Commands ct: _writeCommandRegistry){
                if(ct._command.equals(tagparam)) {
                    return ct;
                }
            }

            return null;
        }
    }
}
