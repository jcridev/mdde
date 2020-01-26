from abc import abstractmethod
from typing import Protocol, List


class PRegistryClient(Protocol):
    @abstractmethod
    def ctrl_set_benchmark_mode(self) -> bool:
        """Set registry state into the state prepared to run benchmark"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_set_shuffle_mode(self) -> bool:
        """Set registry state into the state prepared to shuffle data"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_generate_data(self) -> bool:
        """Generate the data tuples and populate Registry and data nodes"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_run_benchmark(self) -> str:  # TODO: Explicit result type
        """Run benchmark and retrieve the result"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_reset(self) -> bool:
        """Reset the environment"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_get_mode(self) -> str:  # TODO: Explicit result type
        """Get the current Registry state (prepared for benchmark or data shuffle)"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_sync_registry_to_data(self) -> str:  # TODO: Explicit result type
        """Synchronize the current state of the registry to the data nodes"""
        raise NotImplementedError()