from abc import abstractmethod
from typing import Set
try:
    from typing import Protocol
except ImportError:
    from typing_extensions import Protocol

from mdde.registry.container import RegistryResponse


class PRegistryWriteClient(Protocol):
    """
    MDDE Registry client API client protocol
    """

    @abstractmethod
    def write_fragment_create(self, fragment_id: str, tuple_ids: Set[str]) -> RegistryResponse[str]:
        """
        Form a new fragment containing unassigned tuples on a specific node
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
