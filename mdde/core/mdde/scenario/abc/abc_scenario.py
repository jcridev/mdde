import logging
from abc import ABC, abstractmethod
from typing import Tuple, Union, Sequence, Dict

import numpy as np

from mdde.agent.abc import ABCAgent, NodeAgentMapping
from mdde.config import ConfigEnvironment
from mdde.fragmentation.protocol import PFragmenter, PFragmentSorter
from mdde.registry.container import BenchmarkStatus
from mdde.registry.protocol import PRegistryReadClient


class ABCScenario(ABC):
    """Subclass this class overriding provided abstract methods to define a new scenario for the environment"""

    # TODO: Declaration of the meta values (global and local)

    _DEFAULT_NAME = 'Unnamed scenario'

    @abstractmethod
    def __init__(self, scenario_name: str):
        self._scenario_name = scenario_name.strip()
        self._env_config: Union[None, ConfigEnvironment] = None
        self._logger = logging.getLogger('Scenario.{}'.format(self.name.replace(' ', '_')))

        self._nodes_order: Union[None, Tuple[NodeAgentMapping, ...]] = None
        self._actual_fragments: Union[None, Tuple[str, ...]] = None

        self._experiment_id: str = None
        """Experiment ID to which an instance of the scenario is attached to. Provided by the Environment. Guaranteed
        to be filled before any calls to the scenario."""

    def inject_config(self, env_config: ConfigEnvironment) -> None:
        """
        Method called by the core.environment during the initialization. It's guaranteed that this method will be
        called to set the configuration object before any agent specific action is taken.
        :param env_config: MDDE Environment configuration object
        """
        self._env_config = env_config

    def inject_fragments(self, fragments: Tuple[str, ...]) -> None:
        """
        Method called by the core.environment during the initialization.
        :param fragments: Ordered IDs of the created fragments
        """
        self._actual_fragments = fragments

    def attach_to_experiment(self, experiment_id: str) -> None:
        """
        Method is used by the environment to provide the scenario instance with the relevant experiment ID.
        Should not be called by any user defined code.
        :param experiment_id: Short alphanumeric experiment ID.
        """
        self._experiment_id = experiment_id
        for agent in self.get_agents():
            agent.attach_to_experiment(experiment_id)

    @property
    def name(self) -> str:
        """
        Name of the current scenario, used for identification in the results output
        :return: Name as string that's defined in a constructor or default name
        """
        return self._scenario_name if self._scenario_name else self._DEFAULT_NAME

    @property
    def experiment_id(self) -> str:
        """
        Experiment ID to which this scenario is attached to.
        :return: Short alphanumeric string.
        """
        return self._experiment_id

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
    def get_benchmark_num_clients(self) -> int:
        """
        Override this method to return the number of YCSB clients that must be created per benchmark run
        :return: Non-negative, non-zero integer value
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
    def get_fragment_instance_meta_fields(self) -> Union[Sequence[str], None]:
        """
        Override to return a list of meta values that are attached to every instance of a fragment.
        Return None if no meta values needed
        :return:
        """
        raise NotImplementedError

    @abstractmethod
    def get_fragment_global_meta_fields(self) -> Union[Sequence[str], None]:
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

    @abstractmethod
    def do_run_benchmark(self) -> bool:
        """
        Running a benchmark is an expensive operation, so it's reasonable not to run it every step. However, the exact
        frequency of benchmark execution must be decided by the specific scenario.
        Override this method to return True when it's necessary for your scenario to request new statistics from the
        database cluster.

        If this method returns True, after the benchmark run is done, self.process_benchmark_stats() will be invoked, to
        pass the result values of the benchmark to the scenario.

        :return: True - execute benchmark run.
        """
        raise NotImplementedError

    @abstractmethod
    def process_benchmark_stats(self, bench_end_result: BenchmarkStatus) -> None:
        """
        When environment successfully executes a benchmark, the results will be passed to the scenario via this
        method. Override it to process the received statistics any way that makes sense for your scenario.
        :param bench_end_result: Benchmark run results
        """
        raise NotImplementedError

    def flush(self) -> None:
        """
        Override this method if your scenario requires any specific cleaning up (ex. remove temp files, db records)
        before it's initially started and after it finished.
        """
        pass

    def reset(self) -> None:
        """
        Method is called by the Environment when it's being reset. By default, sets the agents done flag to False.
        Override this method if additional cleanup is required.
        """
        for agent in self.get_agents():
            agent.reset()

    def get_observation(self, registry_read: PRegistryReadClient) -> Dict[int, np.ndarray]:
        """
        Observations per client. The default implementation takes in account only allocation and returns it as full
        observation space for each agent.

        Override this method with a custom observation composition that suits your scenario.

        :param registry_read: Read-only client to the registry.
        :return: Dict['agent_id':np.ndarray]
        """
        # retrieve full allocation observation for the scenario
        agent_nodes, fragments, obs = self.get_full_allocation_observation(registry_read=registry_read)
        obs_n: Dict[int, np.ndarray] = {}
        for agent in self.get_agents():
            obs_n[agent.id] = agent.filter_observation(agent_nodes, obs)
        return obs_n

    def get_ordered_nodes(self) -> Tuple[NodeAgentMapping, ...]:
        """
        Make and cash the order of nodes in the scenario.
        :return: Statically ordered Nodes known by the scenario
        """
        if not self._nodes_order:
            self._nodes_order = tuple(na for a in self.get_agents() for na in a.mapped_data_node_ids)
        return self._nodes_order

    def get_full_allocation_observation(self, registry_read: PRegistryReadClient) \
            -> Tuple[Tuple[NodeAgentMapping, ...], Tuple[str, ...], np.ndarray]:
        """
        Generate full fragment allocation observation. Only returns the allocation binary map, actual observation space
        returned to the learners must be shaped in the self.get_observation(...) method.
        Override this method in case you require custom logic of forming full observation space.

        :param registry_read: Read-only client to the registry.
        :return: 1) Ordered IDs of the observed data nodes mapped to agents [agent ID, node ID]
                 2) Ordered list of the fragment IDs corresponding to the x axis of the binary map
                 3) Binary map of fragment allocation (x axis - fragments, y axis - nodes), order of y axis corresponds
                    to the order of IDs in the first return value
        """
        call_result = registry_read.read_get_all_fragments_with_meta(local_meta=None, global_meta=None)
        if call_result.failed:
            raise RuntimeError(call_result.error)
        fragment_catalog = call_result.result
        # make sure fragments are ordered
        obs_frags = fragment_catalog['fragments']
        obs_frags = {v: k for k, v in obs_frags.items()}
        sorted_fragments = self.get_fragment_sorter().sort(list(obs_frags.values()))

        obs_nodes = fragment_catalog['nodes']
        nodes = self.get_ordered_nodes()

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
