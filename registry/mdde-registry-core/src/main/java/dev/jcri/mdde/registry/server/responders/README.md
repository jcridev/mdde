# Responders
It's a layer between the `ICommandParser` implementation and specific CommandHandlers(`IWriteCommandHandler`, `IReadCommandHandler`, `RegistryStateCommandHandler`).

This additional layer allows to wire in behaviour that is common for processing of all handlers, irrespective of implementation.