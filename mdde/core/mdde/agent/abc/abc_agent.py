from abc import ABC, abstractmethod
from typing import Sequence, Union, Tuple, Any
import re

import numpy as np

from mdde.agent.abc import NodeAgentMapping
from mdde.agent.enums import EActionResult
from mdde.config import ConfigEnvironment
from mdde.registry.protocol import PRegistryReadClient, PRegistryWriteClient


class ABCAgent(ABC):
    """
    Base class for the agents definition.
    Every agent must have a unique id assigned in the constructor. Additionally, every agent at runtime has access to
    the read instructions and write API of the registry to facilitate actions execution.
    """

    DEFAULT_GROUP: str = 'agent'
    """Default agent group name."""

    @abstractmethod
    def __init__(self,
                 agent_name: str,
                 agent_id: int,
                 data_node_ids: Union[Sequence[str], str],
                 group: str = DEFAULT_GROUP
                 ):
        """
        Constructor.
        :param agent_name: Agent name (for logging and debugging).
        :param data_node_ids: A set of the data node IDs associated with the agent.
        :param agent_id: Unique integer id assigned to the agent (passed as an id to the learner).
        :param group: Name of the group to which the agent belongs. Only letters and digits are allowed, special
        characters, punctuation and spaces will be stripped.
        """
        if agent_id is None:
            raise TypeError("Agent ID must of type int")
        if data_node_ids is None:
            raise TypeError("Data node ID must of type String")
        self._agent_name: str = agent_name if agent_name else ""
        """Name of the agent. Used for information and logging only"""
        self._agent_id: int = agent_id
        """ID of the agent, must be unique within the current scenario run"""

        if group is None:
            raise TypeError("Agent group can't be None.")
        self._group: str = re.sub('[^A-Za-z0-9_]+', '', group)
        if not self._group:
            raise ValueError("Agent group can't be empty")

        self._filter_obs: bool = True
        """
        If True, agent is expecting to receive full observation space for processing it 
        using :py:func:`filter_observation`.
        If False, 
        """

        self._config: ConfigEnvironment = None
        """Environment configuration"""
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
            self._data_node_id: Tuple[str, ...] = (data_node_ids,)

        # Read and write access to the registry.
        # These properties will have the implementation of the protocols assigned to them at the time of execution,
        # Use these to create actions affecting the registry (write) and the agent observation space (read).
        self._registry_read: PRegistryReadClient = None
        """Read access to the registry. Guaranteed to be filled by the environment before any calls."""
        self._registry_write: PRegistryWriteClient = None
        """Write access to the registry. Guaranteed to be filled by the environment before any calls."""
        self._experiment_id: str = None
        """Experiment ID. Guaranteed to be filled by the environment before any calls."""

        self.done: bool = False
        """'Done flag. Set to True if the agent should no longer do anything within the current episode."""

    @property
    def id(self) -> int:
        """
        Get the ID of the agent, unique within the running scenario.
        :return: Numerical agent ID.
        """
        return self._agent_id

    @property
    def name(self) -> str:
        """
        Get the name of the agent, might be used in logging for simplifying identification.
        :return: String agent name.
        """
        return self._agent_name

    @property
    def experiment_id(self) -> str:
        """
        Experiment ID to which this agent is attached to.
        :return: Short alphanumeric string.
        """
        return self._experiment_id

    @property
    def group(self) -> str:
        """
        Group tittle to which agent belongs.
        :return: String group name. All spaces and special characters are stripped to ensure better compatibility with
        RL frameworks that would use this property.
        """
        return self._group

    def attach_registry(self, registry_read: PRegistryReadClient, registry_write: PRegistryWriteClient) -> None:
        """
        Method is used by the environment to provide agent access to the registry.
        Should not be called by any user defined code.
        :param registry_write: Write access to the registry
        :param registry_read: Read-only access to the registry.
        """
        self._registry_read = registry_read
        self._registry_write = registry_write

    def attach_to_experiment(self, experiment_id: str) -> None:
        """
        Method is used by the environment to provide agent with the relevant experiment ID.
        Should not be called by any user defined code.
        :param experiment_id: Short alphanumeric experiment ID.
        """
        self._experiment_id = experiment_id

    def inject_env_config(self, config: ConfigEnvironment) -> None:
        """
        Environment configuration injected to the agent.
        :param config: ConfigEnvironment
        """
        self._config = config

    def reset(self) -> None:
        """
        Method is called by the Scenario when the Environment being reset.
        By default, sets the agents done flag to False. Override this method if additional cleanup is required.
        """
        self.done = False

    @property
    def obs_filter(self) -> bool:
        """
        If True, scenario should invoke :py:func:`.ABCAgent.filter_observation` to get the agent observations.
        If it's False, :py:func:`.ABCAgent.form_observation` should be used.

        If there are multiple agents having full observation access to the environment, it makes sense to retrieve it
        once in the scenario and then mutate by the agents when needed.
        """
        return self._filter_obs

    @property
    def data_node_ids(self) -> Tuple[str, ...]:
        """
        Get the data node IDs (string) associated with this agent
        :return: Tuple of the data node ID strings managed by the agent
        """
        return self._data_node_id

    @property
    def mapped_data_node_ids(self) -> Tuple[NodeAgentMapping, ...]:
        """Data nodes managed by the agent as NodeAgentMapping tuples
        :return: Ordered tuple of NodeAgentMapping tuples
        """
        return tuple(NodeAgentMapping(self.id, node) for node in self.data_node_ids)

    @abstractmethod
    def get_actions(self) -> int:
        """
        Get the number of actions from 0 to n, each discrete number within the range correspond to a specific action.
        :return: Number of available actions N_a. Each action is mapped to an index within range [0, N_a)
        """
        raise NotImplementedError

    def get_actions_described(self) -> Any:
        """Retrieve meaningful read-only described actions sorted or otherwise conforming to their indexes used by
        the RL algorithms. Generally not useful for the 'proper' DRL. We also don't enforce any specific return
        type as the composition of the actions and the describing object might differ drastically from one agent
        to another. Use with care and only in the specific instances where you're sure it's needed (meta-data, stats,
        etc.). It's also optional for implementation."""
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
    def filter_observation(self,
                           obs_descr: Tuple[NodeAgentMapping, ...],
                           fragments: Tuple[str, ...],
                           obs: np.array) -> np.ndarray:
        """
        Get observation space for the specific agent.
        :param obs_descr: Observation space description.
        :param fragments: Ordered list of fragments.
        :param obs: full_observation: Full observation space provided by the environment.
        :return: Agents can have full or limited observation spaces. In case of the latter, provide the filtering logic
        within this function and return a filtered out observation space.
        """
        raise NotImplementedError

    @abstractmethod
    def form_observation(self, **kwargs) -> np.ndarray:
        """
        Override if the agent should form it's own observations, independent of the full space observations returned
        by the environment.
        Use the read access to the registry in order to form the observation space.

        :param kwargs: (optional) Scenario specific parameters.
        :return: Observation space numpy array.
        """
        raise NotImplementedError
