from abc import ABC, abstractmethod
from typing import Sequence, Union, Tuple, AnyStr
import numpy as np

from mdde.registry.protocol import PRegistryReadClient, PRegistryWriteClient


class ABCAgent(ABC):
    """
    Base class for the agents definition.
    Every agent must have a name (id) assigned in the constructor. Additionally, every agent at runtime has access to
    the read instructions of the
    """
    @abstractmethod
    def __init__(self, agent_id: AnyStr, data_node_ids: Union[Sequence[str], str]):
        """
        Constructor
        :param agent_id: Unique agent id (name)
        :param data_node_ids: A set of the data node IDs associated with the agent
        """
        if agent_id is None:
            raise TypeError("Agent ID must of type String")
        if data_node_ids is None:
            raise TypeError("Data node ID must of type String")

        self._agent_id: AnyStr = agent_id.strip()
        if not self._agent_id:
            raise ValueError("Agent ID can't be empty")

        # At least one data node must be specified
        if len(data_node_ids) < 1:
            raise ValueError("The agent must be associated with at lest one data node")

        if not isinstance(data_node_ids, (str, bytes, bytearray)):
            # Duplicates are not allowed in the data nodes list
            data_node_ids_set = set(data_node_ids)
            if len(data_node_ids_set) != len(data_node_ids):
                raise ValueError("The agent data node ids list contains duplicates")
            self._data_node_id: Tuple[str] = tuple(data_node_ids)
        else:
            self._data_node_id: Tuple[str] = (data_node_ids, )

        # Read and write access to the registry.
        # These properties will have the implementation of the protocols assigned to them at the time of execution,
        # Use these to create actions affecting the registry (write) and the agent observation space (read).
        self._registry_read: Union[PRegistryReadClient, None] = None
        self._registry_write: Union[PRegistryReadClient, None] = None

    def id(self) -> AnyStr:
        """
        Get agent id
        :return: String agent id
        """
        return self._agent_id

    def attach_registry(self, registry_read: PRegistryReadClient, registry_write: PRegistryWriteClient):
        """
        Method is used by the environment to provide agent access to the registry
        :param registry_write: Write access to the registry
        :param registry_read: Read-only access to the registry.
        """
        self._registry_read = registry_read
        self._registry_write = registry_write

    @property
    def get_data_node_ids(self) -> Tuple[str]:
        """
        Get the node ids associated with this agent
        :return:
        """
        return self._data_node_id

    @abstractmethod
    def get_actions(self) -> np.array:
        """
        Get the list of action ids available to the agent
        :return:
        """
        raise NotImplementedError

    @abstractmethod
    def filter_observation(self, full_observation: np.array) -> np.array:
        """
        Get observation space for the specific agent
        :param full_observation: full_observation: Full observation space provided by the environment
        :return: agents can have full or limited observation spaces. In case of the latter, provide the filtering logic
                 within this function and return a filtered out observation space
        """
        raise NotImplementedError
