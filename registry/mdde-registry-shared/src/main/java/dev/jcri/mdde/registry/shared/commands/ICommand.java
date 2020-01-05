package dev.jcri.mdde.registry.shared.commands;

import java.util.List;

public interface ICommand {
    String getCommand();
    List<ExpectedCommandArgument> getExpectedArguments();
}
