package dev.jcri.mdde.registry.control.command.sequential.arguments;

import dev.jcri.mdde.registry.control.ICommands;
import dev.jcri.mdde.registry.control.ReadCommands;
import dev.jcri.mdde.registry.control.WriteCommands;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SimpleSequenceParser implements ISequenceParser {

    private String getCommandOpeningStatement(String command){
        var spaceIdx = command.indexOf(" ");
        if(spaceIdx == -1 ){
            return command;
        }

        return command.substring(0, spaceIdx);
    }

    public ReadCommands getIsReadCommandKeyword(String command) throws UnknownRegistryCommandExceptions{
        return ReadCommands.getCommandTag(getCommandOpeningStatement(command));
    }

    public ReadCommands tryGetIsReadCommandKeyword(String command){
        try {
            return ReadCommands.getCommandTag(getCommandOpeningStatement(command));
        } catch (UnknownRegistryCommandExceptions unknownRegistryCommandExceptions) {
            return null;
        }
    }


    public WriteCommands getIsWriteCommandKeyword(String command) throws UnknownRegistryCommandExceptions{
        return WriteCommands.getCommandTag(getCommandOpeningStatement(command));
    }

    public WriteCommands tryGetIsWriteCommandKeyword(String command){
        try {
            return WriteCommands.getCommandTag(getCommandOpeningStatement(command));
        } catch (UnknownRegistryCommandExceptions unknownRegistryCommandExceptions) {
            return null;
        }
    }

    public List<Object> parseLineArguments(ICommands readCommand, String arguments) throws UnknownRegistryCommandExceptions {
        List<Object> parsedArguments = new ArrayList<>();
        String inProcessString = arguments.trim();
        //  Clear command name
        if(arguments.startsWith(readCommand.getCommand())){
            inProcessString = arguments.substring(readCommand.getCommand().length()).trim();
        }
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
