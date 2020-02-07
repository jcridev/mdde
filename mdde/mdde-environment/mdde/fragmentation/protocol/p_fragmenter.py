from abc import abstractmethod

from mdde.registry.protocol import PRegistryReadClient, PRegistryWriteClient

try:
    from typing import Protocol
except ImportError:
    from typing_extensions import Protocol


class PFragmenter(Protocol):
    """
    Protocol for the implementation of the algorithm controlling the formation of fragments from tuple.
    """
    @abstractmethod
    def run_fragmentation(self, registry_reader: PRegistryReadClient, registry_writer: PRegistryWriteClient) -> bool:
        """
        Override this method with the algorithm performing fragmentation of the tuples that are assigned to nodes in
        the registry but not yet assigned to fragments.
        :param registry_reader: Read access to the registry
        :param registry_writer: Write access to the registry (includes the API calls allowing to perform the formation
        of fragments in the registry)
        :return Must return True if any tuple manipulations were performed except formation of fragments (copies,
        deletions). False, if only READ operations were executed against the registry and the only WRITE operations were
        fragment formation.
        """
        raise NotImplementedError
