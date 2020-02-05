from abc import ABC, abstractmethod
from typing import Set, List

from mdde.registry import PRegistryReadClient


class ABCAgent(ABC):
    """
    Base class for the agents definition.
    Every agent must have a name (id) assigned in the constructor. Additionally, every agent at runtime has access to
    the read instructions of the
    """
    @abstractmethod
    def __init__(self, agent_id: str, data_node_ids: Set[str]):
        """
        Constructor
        :param agent_id: Unique agent id (name)
        :param data_node_ids: A set of the data node IDs associated with the agent
        """
        if agent_id is None:
            raise TypeError("Agent ID must of type String")
        if data_node_ids is None:
            raise TypeError("Data node ID must of type String")

        self._agent_id = agent_id.strip()
        if not self._agent_id:
            raise ValueError("Agent ID can't be empty")

        if len(data_node_ids) < 1:
            raise ValueError("The agent must be associated with at lest one data node")
        self._data_node_id = frozenset(data_node_ids)

        self._registry_read = None

    def id(self) -> str:
        """
        Get agent id
        :return: String agent id
        """
        return self._agent_id

    def attach_registry(self, registry: PRegistryReadClient):
        """
        Method is used by the environment to provide agent access to the registry
        :param registry: Read-only access to the registry.
                         Use the read-only client to
        """
        self._registry_read = registry

    @abstractmethod
    def get_actions(self) -> List[int]:
        """
        Get the list of action ids available to the agent
        :return:
        """
        raise NotImplementedError

