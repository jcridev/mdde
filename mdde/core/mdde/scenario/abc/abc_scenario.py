import logging
from abc import ABC, abstractmethod
from typing import Tuple, Union, Sequence

import numpy as np

from mdde.agent.abc import ABCAgent, NodeAgentMapping
from mdde.fragmentation.protocol import PFragmenter, PFragmentSorter
from mdde.registry.protocol import PRegistryReadClient


class ABCScenario(ABC):
    """
    Subclass this class overriding provided abstract methods to define a new scenario for the environment
    """

    # TODO: Declaration of the agents
    #   TODO: actions definitions
    # TODO: Declaration of the meta values (global and local)

    _DEFAULT_NAME = 'Unnamed scenario'

    @abstractmethod
    def __init__(self, scenario_name: str):
        self._scenario_name = scenario_name.strip()
        self._logger = logging.getLogger('Scenario.{}'.format(self.name.replace(' ', '_')))

    @property
    def name(self) -> str:
        """
        Name of the current scenario, used for identification in the results output
        :return: Name as string that's defined in a constructor or default name
        """
        return self._scenario_name if self._scenario_name else self._DEFAULT_NAME

    @abstractmethod
    def get_benchmark_workload(self) -> str:
        """
        Override this method to return the workload ID you want to be used for benchmark run at the next step
        :return: Workload ID as it's defined in the Registry
        """
        raise NotImplementedError

    @abstractmethod
    def get_data_generator_workload(self) -> str:
        """
        Override this method to return the workload ID you want to be used for data generation
        :return: Workload ID as it's defined in the Registry
        """
        raise NotImplementedError

    @abstractmethod
    def get_agents(self) -> Tuple[ABCAgent]:
        """
        Override this method to return the list of agents that are to be used within the environment. Make sure that
        agents have unique IDs and correspond to existing data nodes
        :return:
        """
        raise NotImplementedError

    @abstractmethod
    def get_fragmenter(self) -> PFragmenter:
        """
        Override to return a class implementing
        :return:
        """
        raise NotImplementedError

    @abstractmethod
    def get_fragment_sorter(self) -> PFragmentSorter:
        """
        Override to return an instance of a fragment sorter
        :return: PFragmentSorter
        """
        raise NotImplementedError

    @abstractmethod
    def get_fragment_instance_meta_fields(self) -> Union[Sequence, None]:
        """
        Override to return a list of meta values that are attached to every instance of a fragment.
        Return None if no meta values needed
        :return:
        """
        raise NotImplementedError

    @abstractmethod
    def get_fragment_global_meta_fields(self) -> Union[Sequence, None]:
        """
        Override to return a list of meta values that are attached to a fragment globally.
        Return None if no meta values needed
        :return:
        """
        raise NotImplementedError

    def get_full_allocation_observation(self, registry_read: PRegistryReadClient) \
            -> Tuple[Tuple[NodeAgentMapping, ...], np.array]:
        """
        Generate full observation space of the scenario.
        Override this method in case you require custom logic of forming full observation space.
        :param registry_read: Read-only client to the registry.
        :return: 1) Ordered IDs of the observed data nodes mapped to agents [agent ID, node ID]
                 2) Binary map of fragment allocation (x axis - fragments, y axis - nodes), order of y axis corresponds
                    to the order of IDs in the first return value
        """
        # TODO: Meta values
        call_result = registry_read.read_get_all_fragments_with_meta(local_meta=None, global_meta=None)
        if call_result.failed:
            raise RuntimeError(call_result.error)
        fragment_catalog = call_result.result
        # make sure fragments are ordered
        obs_frags = fragment_catalog['fragments']
        obs_frags = {v: k for k, v in obs_frags.items()}
        sorted_fragments = self.get_fragment_sorter().sort(list(obs_frags.values()))

        obs_nodes = fragment_catalog['nodes']
        nodes = tuple(na for a in self.get_agents() for na in a.mapped_data_node_ids)

        obs_full = np.zeros((len(nodes), len(sorted_fragments)), dtype=np.bool)
        for node in nodes:
            obs_node_id = obs_nodes.get(node.node_id, None)
            if obs_node_id is None:
                self._logger.warning("Node: '{}',  mapped to agent '{}' is not part of the returned observation space.",
                                     node.node_id, node.agent_id)
                continue
            obs_node_frags = fragment_catalog['nodefrags'].get(obs_node_id, None)
            if obs_node_frags is None:
                self._logger.warning("No observations were received for node: '{}',  mapped to agent '{}'.",
                                     node.node_id, node.agent_id)
                continue
            n_y = nodes.index(node)
            n_exist_val = np.ones((len(obs_node_frags),), dtype=np.bool)
            n_exists_pos = list()
            for obs_node_frag in obs_node_frags:
                obs_node_frag_id = obs_frags.get(obs_node_frag)
                obs_node_frag_id_x = sorted_fragments.index(obs_node_frag_id)
                n_exists_pos.append((n_y, obs_node_frag_id_x))
            rows, cols = zip(*n_exists_pos)
            obs_full[rows, cols] = n_exist_val

        return nodes, obs_full
