package dev.jcri.mdde.registry.control;

import java.util.List;

public interface ICommands {
    String getCommand();
    List<ExpectedCommandArgument> getExpectedArguments();
}
