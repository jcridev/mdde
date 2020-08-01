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
    MDDE Registry client API client protocol.
    """

    @abstractmethod
    def ctrl_set_benchmark_mode(self) -> RegistryResponse[bool]:
        """
        Set registry state into the state prepared to run benchmark.
        :return: Success flag.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_set_shuffle_mode(self) -> RegistryResponse[bool]:
        """
        Set registry state into the state prepared to shuffle data.
        :return: Success flag.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_populate_default_nodes(self) -> RegistryResponse[Set[str]]:
        """
        Populate nodes that are marked as default in the empty registry.
        :return: Set of default nodes IDs which were added to the registry from the configuration file.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_generate_data(self, workload_id: str) -> RegistryResponse[bool]:
        """
        Generate the data tuples and populate Registry and data nodes.
        :return: Success flag.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_start_benchmark(self, workload_id: str, num_workers: int) -> RegistryResponse[Dict]:  # TODO: Explicit result type
        """
        Start benchmark run.
        :return: Response of the registry on starting the benchmark (Not the benchmark statistics, that should be
        retrieved with :func:`ctrl_get_benchmark`.).
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_get_benchmark(self) -> RegistryResponse[BenchmarkStatus]:
        """
        Get the latest benchmark run state.
        :return: Status of the latest benchmark run and collected statistics if it was finished.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_init_benchmark_counterfeit(self) -> RegistryResponse[bool]:
        """
        Estimation benchmark: initialize based on the latest benchmark execution stats.
        :return: Success flag.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_get_benchmark_counterfeit(self,
                                       magnitude_range_start: float,
                                       magnitude_range_end: float) -> RegistryResponse[BenchmarkStatus]:
        """
        Get a benchmark estimation based on the latest benchmark run and current allocation state.
        :param magnitude_range_start: Magnitude range start of adjustment for the change in benchmark estimation.
        :param magnitude_range_end: Magnitude range end of adjustment for the change in benchmark estimation.

        :return: Results of counterfeit benchmark (estimated benchmark statistics based on the current allocation and
        previously collected YCSB benchmark parameters).
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_reset(self) -> RegistryResponse[bool]:
        """Reset the environment to the latest snapshot."""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_flush(self) -> RegistryResponse[bool]:
        """
        Flush the environment. In contrast to reset, this command erases all data and all snapshots from the
        environment, basically allowing to start the experiments from a clean slate.
        :return: Success flag.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_get_mode(self) -> RegistryResponse[ERegistryMode]:
        """Get the current Registry state (prepared for benchmark or data shuffle)."""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_sync_registry_to_data(self) -> RegistryResponse[bool]:
        """
        Synchronize the current state of the registry to the data nodes.
        :return: Success flag.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_snapshot_create(self, as_default: bool) -> RegistryResponse[str]:
        """
        Create a new snapshot, saving the current state of the registry and data nodes.
        :param as_default: True - set as default, meaning it'll be used the next time reset is called.
        :return: ID of the snapshot.
        """
        raise NotImplementedError()

    @abstractmethod
    def ctrl_snapshot_restore(self, snap_id: str) -> RegistryResponse[bool]:
        """
        Create a new snapshot, saving the current state of the registry and data nodes
        :param snap_id: ID of the snapshot that should be restored.
        :return: True - snapshot was restored successfully.
        """
        raise NotImplementedError()

