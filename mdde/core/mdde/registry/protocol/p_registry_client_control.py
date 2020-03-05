from abc import abstractmethod
from typing import Dict, Set

from mdde.registry.enums import ERegistryMode

try:
    from typing import Protocol
except ImportError:
    from typing_extensions import Protocol

from mdde.registry.container import RegistryResponse, BenchmarkStatus


class PRegistryControlClient(Protocol):
    """
    MDDE Registry client API client protocol
    """

    @abstractmethod
    def ctrl_set_benchmark_mode(self) -> RegistryResponse[bool]:
        """Set registry state into the state prepared to run benchmark"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_set_shuffle_mode(self) -> RegistryResponse[bool]:
        """Set registry state into the state prepared to shuffle data"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_populate_default_nodes(self) -> RegistryResponse[Set[str]]:
        """Populate nodes that are marked as default in the empty registry"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_generate_data(self, workload_id: str) -> RegistryResponse[bool]:
        """Generate the data tuples and populate Registry and data nodes"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_start_benchmark(self, workload_id: str) -> RegistryResponse[Dict]:  # TODO: Explicit result type
        """Run benchmark and retrieve the result"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_get_benchmark(self) -> RegistryResponse[BenchmarkStatus]:
        """Get the latest benchmark run state"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_reset(self) -> RegistryResponse[bool]:
        """Reset the environment to the latest snapshot"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_flush(self) -> RegistryResponse[bool]:
        """
        Flush the environment. In contrast to reset, this command erases all data and all snapshots from the
        environment, basically allowing to start the experiments from a clean slate.
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_get_mode(self) -> RegistryResponse[ERegistryMode]:
        """Get the current Registry state (prepared for benchmark or data shuffle)"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_sync_registry_to_data(self) -> RegistryResponse[bool]:
        """Synchronize the current state of the registry to the data nodes"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_snapshot_create(self, as_default: bool) -> RegistryResponse[str]:
        """
        Create a new snapshot, saving the current state of the registry and data nodes
        :param as_default: True - set as default, meaning it'll be used the next time reset is called
        :return: ID of the snapshot
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_snapshot_restore(self, snap_id: str) -> RegistryResponse[bool]:
        """
        Create a new snapshot, saving the current state of the registry and data nodes
        :param snap_id: ID of the snapshot that should be restored.
        :return: True - snapshot was restored successfully
        """
        raise NotImplementedError()

