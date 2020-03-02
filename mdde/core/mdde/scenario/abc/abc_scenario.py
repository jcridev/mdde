import logging
from abc import ABC, abstractmethod
from typing import Tuple, Union, Sequence, Dict
import time

import numpy as np

from mdde.agent.abc import ABCAgent, NodeAgentMapping
from mdde.fragmentation.protocol import PFragmenter, PFragmentSorter
from mdde.registry.protocol import PRegistryReadClient, PRegistryControlClient


class ABCScenario(ABC):
    """
    Subclass this class overriding provided abstract methods to define a new scenario for the environment
    """

    # TODO: Declaration of the meta values (global and local)
    # TODO: Reward function definition

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

    @abstractmethod
    def make_collective_step(self, actions: Dict[int, int]) -> None:
        """
        Override to implement an action execution for a dictionary of agents coupled with the ids of their respective
        actions chosen by the learner.

        Note: Reward is not returned right away and has to be explicitly requested by calling self.get_reward().
        :param actions: Dict['agent_id', 'action id from the agent's action space']
        """
        raise NotImplementedError

    @abstractmethod
    def get_reward(self) -> Dict[int, float]:
        """
        Override to return a dictionary of rewards per agent.

        :return: Dict['agent_id', floating point reward]
        """
        raise NotImplementedError

    def get_observation(self, registry_read: PRegistryReadClient) -> Dict[int, np.ndarray]:
        """
        Observations per client. Take in account only allocation, override this method with the observation composition
        you require.

        :param registry_read: Read-only client to the registry.
        :return: Dict['agent_id':np.ndarray]
        """
        # retrieve full observation space for the scenario
        agent_nodes, fragments, obs = self.get_full_allocation_observation(registry_read=registry_read)
        obs_n: Dict[int, np.ndarray] = {}
        for agent in self.get_agents():
            obs_n[agent.id] = agent.filter_observation(agent_nodes, obs)
        return obs_n

    def _benchmark(self, registry_control: PRegistryControlClient):
        bench_start_result = registry_control.ctrl_start_benchmark(workload_id=self.get_benchmark_workload())
        if bench_start_result.failed:
            raise RuntimeError(bench_start_result.error)
        while True:
            time.sleep(15)
            bench_status = registry_control.ctrl_get_benchmark()
            if bench_status.failed:
                raise RuntimeError(bench_status.error)
            t = bench_status.result
            if bench_status.result['completed']:
                break
        # TODO: Proper return value

    def get_full_allocation_observation(self, registry_read: PRegistryReadClient) \
            -> Tuple[Tuple[NodeAgentMapping, ...], Tuple[str, ...], np.ndarray]:
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

        obs_dtype = np.int8  # https://numpy.org/devdocs/user/basics.types.html
        obs_full = np.zeros((len(nodes), len(sorted_fragments)), dtype=obs_dtype)
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
            n_exist_val = np.ones((len(obs_node_frags),), dtype=obs_dtype)
            n_exists_pos = list()
            for obs_node_frag in obs_node_frags:
                obs_node_frag_id = obs_frags.get(obs_node_frag)
                obs_node_frag_id_x = sorted_fragments.index(obs_node_frag_id)
                n_exists_pos.append((n_y, obs_node_frag_id_x))
            rows, cols = zip(*n_exists_pos)
            obs_full[rows, cols] = n_exist_val

        return nodes, sorted_fragments, obs_full
