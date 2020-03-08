from abc import ABC, abstractmethod
from typing import Sequence, Union, Tuple, AnyStr
import numpy as np

from mdde.agent.abc import NodeAgentMapping
from mdde.agent.enums import EActionResult
from mdde.registry.protocol import PRegistryReadClient, PRegistryWriteClient


class ABCAgent(ABC):
    """
    Base class for the agents definition.
    Every agent must have a name (id) assigned in the constructor. Additionally, every agent at runtime has access to
    the read instructions of the
    """
    @abstractmethod
    def __init__(self,
                 agent_name: AnyStr,
                 agent_id: int,
                 data_node_ids: Union[Sequence[str], str]
                 ):
        """
        Constructor
        :param agent_name: Agent name (for logging and debugging)
        :param data_node_ids: A set of the data node IDs associated with the agent
        :param agent_id Unique integer id assigned to the agent (passed as an id to the learner)
        """
        if agent_id is None:
            raise TypeError("Agent ID must of type int")
        if data_node_ids is None:
            raise TypeError("Data node ID must of type String")
        self._agent_name: agent_name
        self._agent_id: int = agent_id

        # At least one data node must be specified
        if len(data_node_ids) < 1:
            raise ValueError("The agent must be associated with at lest one data node")

        if not isinstance(data_node_ids, (str, bytes, bytearray)):
            # Duplicates are not allowed in the data nodes list
            data_node_ids_set = set(data_node_ids)
            if len(data_node_ids_set) != len(data_node_ids):
                raise ValueError("The agent data node ids list contains duplicates")
            self._data_node_id: Tuple[str, ...] = tuple(data_node_ids)
        else:
            self._data_node_id: Tuple[str, ...] = (data_node_ids, )

        # Read and write access to the registry.
        # These properties will have the implementation of the protocols assigned to them at the time of execution,
        # Use these to create actions affecting the registry (write) and the agent observation space (read).
        self._registry_read: Union[PRegistryReadClient, None] = None
        self._registry_write: Union[PRegistryWriteClient, None] = None

    @property
    def id(self) -> int:
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
    def get_data_node_ids(self) -> Tuple[str, ...]:
        """
        Get the node ids associated with this agent
        :return:
        """
        return self._data_node_id

    @property
    def mapped_data_node_ids(self) -> Tuple[NodeAgentMapping, ...]:
        return tuple(NodeAgentMapping(self.id, node) for node in self.get_data_node_ids)

    @abstractmethod
    def get_actions(self) -> int:
        """
        Get the number of actions from 0 to n, each discrete number within the range correspond to a specific action.
        :return: Number of available actions N_a. Each action is mapped to an index within range [0, N_a)
        """
        raise NotImplementedError

    @abstractmethod
    def create_action_space(self,
                            nodes: Tuple[NodeAgentMapping, ...],
                            fragments: Sequence[str],
                            obs: np.ndarray
                            ) -> int:
        """
        Override this method to create action space associated with this agent.
        When this method is invoked, attach_registry() method was already called so the agent already should have access
        to the registry. Additionally, this method is parameterized with the full observation space that was generated
        right after the initialization of the environment.

        Actions provided by the agent can rely on the supplied parameters or be hard coded. Implement this method in
        accordance to the simulated scenario.
        :param nodes: Description of the observation space nodes
        :param fragments: Ordered sequence of fragments
        :param obs: Observation space
        :return: Number of available actions N_a. Each action is mapped to an index within range [0, N_a)
        """
        raise NotImplementedError

    @abstractmethod
    def do_action(self, action_id: int) -> EActionResult:
        """
        Execute an action corresponding to the specified action id [0,self.get_actions())
        :param action_id: Action id as defined in the action space
        :return: EActionResult for an action that was processed correctly. If a general error (error without an MDDE
        error code is returned, then a general exception must be raised instead)
        """
        raise NotImplementedError

    @abstractmethod
    def filter_observation(self, obs_descr: Tuple[NodeAgentMapping, ...], obs: np.array) -> np.ndarray:
        """
        Get observation space for the specific agent
        :param obs_descr: Observation space description
        :param obs: full_observation: Full observation space provided by the environment
        :return: agents can have full or limited observation spaces. In case of the latter, provide the filtering logic
        within this function and return a filtered out observation space
        """
        raise NotImplementedError
