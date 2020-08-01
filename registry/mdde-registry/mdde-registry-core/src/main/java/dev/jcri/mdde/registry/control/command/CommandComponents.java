package dev.jcri.mdde.registry.control.command;

import dev.jcri.mdde.registry.shared.commands.EReadCommand;
import dev.jcri.mdde.registry.shared.commands.EStateControlCommand;
import dev.jcri.mdde.registry.shared.commands.EWriteCommand;
import dev.jcri.mdde.registry.store.exceptions.UnknownRegistryCommandExceptions;

import java.util.NoSuchElementException;

/**
 * Container class for the base components of the incoming command.
 * @param <T> Arguments portion type.
 */
public class CommandComponents<T> {
    /**
     * Command keyword (should correspond to such in ICommand descendants).
     */
    private String _keyword;
    /**
     * Arguments (not deserialized).
     */
    private T _args;

    /**
     * Constructor.
     * @param keyword Command keyword (should correspond to such in ICommand descendants).
     * @param arguments Arguments (not deserialized).
     */
    public CommandComponents(String keyword, T arguments){
        if(keyword == null || keyword.isEmpty()){
            throw new IllegalArgumentException("keyword can't be null or empty");
        }

        _keyword = keyword;
        _args = arguments;
    }

    /**
     * Get command keyword. Should correspond to one of the ICommand implementations (EReadCommand, EWriteCommand).
     * @return Command keyword string value.
     */
    public String getKeyword() {
        return _keyword;
    }

    /**
     * Assign a textual command keyword.
     * @param keyword Command keyword string value.
     */
    public void setKeyword(String keyword) {
        this._keyword = keyword;
    }

    /**
     * Get command arguments in the received serialized state.
     * @return Arguments part of the statement.
     */
    public T getArgs() {
        return _args;
    }

    /**
     * Set arguments.
     * @param args Arguments.
     */
    public void setArgs(T args) {
        this._args = args;
    }

    /**
     * Attempt to get the read command defined in registry corresponding to the string keyword.
     * @return Read command from the catalog with the description.
     * @throws UnknownRegistryCommandExceptions Keyword is no a read command.
     */
    public EReadCommand getIsReadCommandKeyword() throws UnknownRegistryCommandExceptions {
        return EReadCommand.getCommandTag(getKeyword());
    }

    /**
     * Attempt to get the read command defined in registry corresponding to the string keyword.
     * @return Read command object if keyword corresponds to a defined read command, null otherwise.
     */
    public EReadCommand tryGetIsReadCommandKeyword(){
        try {
            return EReadCommand.getCommandTag(getKeyword());
        } catch (NoSuchElementException unknownRegistryCommandExceptions) {
            return null;
        }
    }

    /**
     * Attempt to get the write command defined in registry corresponding to the string keyword.
     * @return Write command from the catalog with the description.
     * @throws UnknownRegistryCommandExceptions Keyword is no a write command.
     */
    public EWriteCommand getIsWriteCommandKeyword() throws UnknownRegistryCommandExceptions{
        return EWriteCommand.getCommandTag(getKeyword());
    }

    /**
     * Attempt to get the write command defined in registry corresponding to the string keyword.
     * @return Write command object if keyword corresponds to a defined write command, null otherwise.
     */
    public EWriteCommand tryGetIsWriteCommandKeyword(){
        try {
            return EWriteCommand.getCommandTag(getKeyword());
        } catch (NoSuchElementException unknownRegistryCommandExceptions) {
            return null;
        }
    }

    /**
     * Attempt to get the state control command defined in registry corresponding to the string keyword.
     * @return State control command from the catalog with the description.
     * @throws UnknownRegistryCommandExceptions Keyword is no a state control command.
     */
    public EStateControlCommand getIsStateControlCommandKeyword() throws UnknownRegistryCommandExceptions{
        return EStateControlCommand.getCommandTag(getKeyword());
    }

    /**
     * Attempt to get the state control command defined in registry corresponding to the string keyword.
     * @return State control command object if keyword corresponds to a defined state control command, null otherwise.
     */
    public EStateControlCommand tryGetIsStateControlCommandKeyword(){
        try {
            return EStateControlCommand.getCommandTag(getKeyword());
        } catch (NoSuchElementException unknownRegistryCommandExceptions) {
            return null;
        }
    }
}
