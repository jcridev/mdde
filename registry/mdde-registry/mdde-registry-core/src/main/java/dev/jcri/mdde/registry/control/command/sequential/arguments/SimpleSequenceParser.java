package dev.jcri.mdde.registry.control.command.sequential.arguments;

import dev.jcri.mdde.registry.control.command.CommandComponents;
import dev.jcri.mdde.registry.shared.commands.ICommand;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Parses command incoming as textual sequences such as "COMMAND ARG_0 ARG_2 ...". Order of arguments depends on
 * such defined in ICommand descendants.
 */
public class SimpleSequenceParser implements ISequenceParser {
    /**
     * Get the command keyword from the statement line.
     * @param command Keyword + args string.
     * @return Command keyword string.
     */
    private String getCommandOpeningStatement(String command){
        var spaceIdx = command.indexOf(" ");
        if(spaceIdx == -1 ){
            return command;
        }

        return command.substring(0, spaceIdx);
    }

    /**
     * Get a container with the keyword and args separated.
     * @param command Full command to split.
     * @return Split command string container.
     */
    @Override
    public CommandComponents<String> getArgumentsFromLine(String command) {
        var keyword = getCommandOpeningStatement(command);
        var arguments = command.substring(keyword.length()).trim();

        return new CommandComponents<>(keyword, arguments);
    }

    /**
     * Get an ordered list of parsed arguments from the string of arguments for the specific command keyword.
     * @param thisCommand Command keyword from the internal catalog.
     * @param arguments Use getArgumentsFromLine to get this argument.
     * @return Ordered list of arguments parsed from string to Object according to the command keyword scheme.
     * @throws UnknownRegistryCommandExceptions The command is unknown or statement is malformed.
     */
    @Override
    public List<Object> parseLineArguments(ICommand thisCommand,
                                           String arguments)
            throws UnknownRegistryCommandExceptions {
        List<Object> parsedArguments = new ArrayList<>();
        String inProcessString = arguments.trim();
        Character currentDelimiter = ' ';
        while(inProcessString.length() > 0){
            // Check for a set
            if(inProcessString.startsWith("[")){
                var setEndIdx =  inProcessString.indexOf("]");
                if(setEndIdx == -1){
                    throw new UnknownRegistryCommandExceptions("Found opening set '[' but no closing bracket ']'.");
                }
                var setString = inProcessString.substring(1, setEndIdx);
                inProcessString = inProcessString.substring(setEndIdx).trim();
                var items = setString.split(",");
                var set = new HashSet<String>();
                var added = set.addAll(Arrays.asList(items));
                if(!added){
                    throw new UnknownRegistryCommandExceptions("Passed argument set contains non-unique items.");
                }
                parsedArguments.add(set);
                continue;
            }
            // Check for a string
            if(inProcessString.startsWith("\"")){
                var setEndIdx =  inProcessString.indexOf("\"");
                if(setEndIdx == -1){
                    throw new UnknownRegistryCommandExceptions("Found opening for a string '\"' but no closing '\"'.");
                }
                var stringParam = inProcessString.substring(1, setEndIdx);
                if(stringParam.isEmpty()){
                    throw new UnknownRegistryCommandExceptions("Empty string arguments are not allowed.");
                }
                inProcessString = inProcessString.substring(setEndIdx).trim();
                parsedArguments.add(stringParam);
                continue;
            }
            throw new UnknownRegistryCommandExceptions("Only sets ([...]) and strings (\"...\") are allowed as arguments.");
        }
        return parsedArguments;
    }
}
