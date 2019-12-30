package dev.jcri.mdde.registry.control;

import java.util.List;

public interface ICommand {
    String getCommand();
    List<ExpectedCommandArgument> getExpectedArguments();
}
