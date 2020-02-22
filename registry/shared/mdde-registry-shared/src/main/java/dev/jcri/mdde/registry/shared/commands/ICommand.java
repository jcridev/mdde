package dev.jcri.mdde.registry.shared.commands;

import java.util.List;

/**
 * MDDE Registry command
 */
public interface ICommand {
    /**
     * Get command tag used to invoke the command by the command processor.
     * @return String command tag
     */
    String getCommand();

    /**
     * Get a full textual description of the command that could be returned to the user.
     * @return
     */
    default String getDescription(){
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Command: ").append(this.getCommand()).append(". Expected arguments: ");
        strBuilder.append(getExpectedArguments().size());
        if(getExpectedArguments().size() > 0){
            strBuilder.append(" [");
            for (int i = 0; i < getExpectedArguments().size(); i++) {
                ExpectedCommandArgument arg = getExpectedArguments().get(i);
                strBuilder.append(i).append(": ").append(arg.toString()).append("; ");
            }
            strBuilder.append("]");
        }

        return strBuilder.toString();
    }

    /**
     * Get the ordered list of the arguments expected by this command.
     * @return
     */
    List<ExpectedCommandArgument> getExpectedArguments();
}
