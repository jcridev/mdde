from abc import abstractmethod
from typing import Protocol, List, Set, Dict

from mdde.registry.RegistryResponse import RegistryResponse


class PRegistryClient(Protocol):
    """
    MDDE Registry client API client protocol
    """

    # Registry state control operations

    @abstractmethod
    def ctrl_set_benchmark_mode(self) -> RegistryResponse[bool]:
        """Set registry state into the state prepared to run benchmark"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_set_shuffle_mode(self) -> RegistryResponse[bool]:
        """Set registry state into the state prepared to shuffle data"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_generate_data(self) -> RegistryResponse[bool]:
        """Generate the data tuples and populate Registry and data nodes"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_start_benchmark(self, workload_id: str) -> RegistryResponse[Dict]:  # TODO: Explicit result type
        """Run benchmark and retrieve the result"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_get_benchmark(self) -> RegistryResponse[Dict]:
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
    def ctrl_get_mode(self) -> RegistryResponse[Dict]:  # TODO: Explicit result type
        """Get the current Registry state (prepared for benchmark or data shuffle)"""
        raise NotImplementedError()

    @abstractmethod
    def ctrl_sync_registry_to_data(self) -> RegistryResponse[bool]:  # TODO: Explicit result type
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

    # READ operations

    @abstractmethod
    def read_everything(self) -> RegistryResponse[str]:
        """
        Retrieve the entire registry state including all of the fragments and tuples for all of the nodes.
        *The function is primarily meant for debugging.*
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def read_find_fragment(self, fragment_id: str) -> RegistryResponse[str]:  # TODO: Explicit type
        """
        Get nodes where the fragment is located
        :param fragment_id: Fragment ID
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def read_get_all_fragments_with_meta(self, local_meta: Set[str], global_meta: Set[str]) -> RegistryResponse[Dict]:  # TODO: Explicit type
        """
        A complete catalog of fragments with optional meta values
        :param local_meta: Exemplar bound meta values for fragments
        :param global_meta: Global meta values for fragments
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def read_count_fragment(self, fragment_id: str) -> RegistryResponse[int]:
        """
        Retrieve how many copies of a specific fragment exists in the registry
        :param fragment_id: Fragment ID
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def read_nodes(self) -> RegistryResponse[str]:  # TODO: Explicit type
        """
        Retrieve nodes IDs from the registry
        """
        raise NotImplementedError()

    @abstractmethod
    def read_node_unassigned_tuples(self, node_id: str) -> RegistryResponse[Set[str]]:
        """
        Retrieve ids of the tuples located on the nodes that aren't assigned ot a fragment
        :param node_id: Node ID
        :return: Set of tuple IDs
        """
        raise NotImplementedError()

    @abstractmethod
    def read_node_fragments(self, node_id: str) -> RegistryResponse[Set[str]]:
        """
        Get fragments located on the specified node
        :param node_id: Node ID
        :return: Set of fragment IDs
        """
        raise NotImplementedError()

    @abstractmethod
    def read_fragment_meta_on_exemplar(self, fragment_id: str, node_id: str, meta_tag: str) -> RegistryResponse[str]:
        """
        Get value of a meta tag attached to a specific exemplar of the fragment
        :param fragment_id: Fragment ID
        :param node_id: Node ID where the fragment is located
        :param meta_tag: Meta tag
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def read_fragment_meta_global(self, fragment_id: str, meta_tag: str) -> RegistryResponse[str]:
        """
        Get value of a meta tag attached to a fragment globally
        :param fragment_id: Fragment ID
        :param meta_tag: Meta tag
        :return:
        """
        raise NotImplementedError()

    # WRITE operations

    @abstractmethod
    def write_fragment_create(self, fragment_id: str, tuple_ids: Set[str], node_id: str) -> RegistryResponse[str]:
        """
        Form a new fragment containing unassigned tuples on a specific node
        :param node_id: Node ID where the fragment is allocated
        :param fragment_id:  Id of a newly created fragment
        :param tuple_ids: Tuple IDs that should be included in the fragment
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def write_fragment_append_tuple(self, fragment_id: str, tuple_id: str) -> RegistryResponse[bool]:
        """
        Append an unassigned tuple ID to existing fragment.

        :param fragment_id:
        :param tuple_id: ID of an unassigned tuple.
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def write_fragment_replicate(self, fragment_id: str, source_node_id: str, destination_node_id: str):
        """
        Replicate (COPY) fragment from one node to another
        :param fragment_id: ID of the fragment to be copied
        :param source_node_id: Source node where an exemplar of the fragment is currently located
        :param destination_node_id: Destination node where the fragment should be copied but not yet present
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def write_fragment_delete_exemplar(self, fragment_id: str, node_id: str) -> RegistryResponse[str]:
        """
        Remove a copy of a fragment from the specified node.
        It's only possible to remove a copy of a fragment if there is at least one copy still present on some other
        node than the specified node
        :param fragment_id: ID of the fragment to be removed from the specified node
        :param node_id: ID of the node from which the fragment should be removed
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def write_fragment_meta_on_copy(self, fragment_id: str,
                                    node_id: str,
                                    meta_tag: str,
                                    meta_value: str) -> RegistryResponse[bool]:
        """
        Set a META value to a specific fragment copy.
        The value will be specific to this specific copy. It's copied if the fragment is replicated.
        :param fragment_id: ID of the fragment
        :param node_id: ID of the node where the specific fragment copy is located
        :param meta_tag: Tag (name) of the meta field
        :param meta_value: Meta value serialized into a string
        :return:
        """
        raise NotImplementedError()

    @abstractmethod
    def write_fragment_meta_global(self, fragment_id: str, meta_tag: str, meta_value: str) -> RegistryResponse[bool]:
        """
        Set a META value to a fragment globally.
        The value will be attacked to the global fragment ID, not a specific copy of it
        :param fragment_id: ID of the fragment
        :param meta_tag: Name of the meta field
        :param meta_value: Meta value serialized into a string
        :return:
        """
        raise NotImplementedError()
