# Lock base abstract implementation

`ReadCommandHandler` and `WriteCommandHandler` are abstract classes implementing `IReadCommandHandler` and `IWriteCommandHandler` interfaces respectively.
These are abstract classes simplifying implementation of a different storage medium for the registry data.

Abstract `ReadCommandHandler` and `WriteCommandHandler` are designed in a way that you need to provide implementation for low level storage medium interaction. Constraints are already taken care of.

However, if the lock based implementation is too restrictive or not performing well enough, provide your own implementation of `IReadCommandHandler` and `IWriteCommandHandler`, without extending `ReadCommandHandler` and `WriteCommandHandler`.