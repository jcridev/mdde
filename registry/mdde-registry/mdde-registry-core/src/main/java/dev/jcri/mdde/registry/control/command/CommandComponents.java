package dev.jcri.mdde.registry.control.command;

import dev.jcri.mdde.registry.shared.commands.EReadCommand;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.NoSuchElementException;

/**
 * Container class for the base components of the incoming command
 * @param <T>
 */
public class CommandComponents<T> {
    private String _keyword;
    private T _args;

    public CommandComponents(String keyword, T arguments){
        if(keyword == null || keyword.isEmpty()){
            throw new IllegalArgumentException("keyword can't be null or empty");
        }

        _keyword = keyword;
        _args = arguments;
    }

    /**
     * Get command keyword. Should correspond to one of the ICommand implementations (EReadCommand, EWriteCommand)
     * @return
     */
    public String getKeyword() {
        return _keyword;
    }

    public void setKeyword(String keyword) {
        this._keyword = keyword;
    }

    /**
     * Get command arguments in the received serialized state
     * @return
     */
    public T getArgs() {
        return _args;
    }

    public void setArgs(T args) {
        this._args = args;
    }


    public EReadCommand getIsReadCommandKeyword() throws UnknownRegistryCommandExceptions {
        return EReadCommand.getCommandTag(getKeyword());
    }

    public EReadCommand tryGetIsReadCommandKeyword(){
        try {
            return EReadCommand.getCommandTag(getKeyword());
        } catch (NoSuchElementException unknownRegistryCommandExceptions) {
            return null;
        }
    }

    public EWriteCommand getIsWriteCommandKeyword() throws UnknownRegistryCommandExceptions{
        return EWriteCommand.getCommandTag(getKeyword());
    }

    public EWriteCommand tryGetIsWriteCommandKeyword(){
        try {
            return EWriteCommand.getCommandTag(getKeyword());
        } catch (NoSuchElementException unknownRegistryCommandExceptions) {
            return null;
        }
    }

    public EStateControlCommand getIsStateControlCommandKeyword() throws UnknownRegistryCommandExceptions{
        return EStateControlCommand.getCommandTag(getKeyword());
    }

    public EStateControlCommand tryGetIsStateControlCommandKeyword(){
        try {
            return EStateControlCommand.getCommandTag(getKeyword());
        } catch (NoSuchElementException unknownRegistryCommandExceptions) {
            return null;
        }
    }
}
