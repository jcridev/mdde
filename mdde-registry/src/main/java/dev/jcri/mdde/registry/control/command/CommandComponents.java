package dev.jcri.mdde.registry.control.command;

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
     * Get command keyword. Should correspond to one of the ICommand implementations (ReadCommand, WriteCommand)
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
}
