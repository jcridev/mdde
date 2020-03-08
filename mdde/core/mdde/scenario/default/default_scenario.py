from typing import Tuple, Union, Sequence, Dict

import numpy as np
import tiledb
import pathlib

from mdde.agent.abc import ABCAgent
from mdde.config import ConfigEnvironment
from mdde.fragmentation.default import DefaultFragmenter, DefaultFragmentSorter
from mdde.fragmentation.protocol import PFragmentSorter, PFragmenter
from mdde.registry.container import BenchmarkStatus
from mdde.registry.protocol import PRegistryReadClient
from mdde.scenario.abc import ABCScenario


class DefaultScenario(ABCScenario):
    """
    Full observation space multi-agent data distribution scenario with read-only workload
    """

    def __init__(self, num_fragments: int, agents: Sequence[ABCAgent]):
        super().__init__('Default scenario')
        self._default_workload = 'read10000'

        self._num_fragments: int = num_fragments
        self._agents: Tuple[ABCAgent, ...] = tuple(agents)

        self.__tiledb_group_name = 'def_tdb_arrays'
        self.__tiledb_stats_array = None

    def inject_config(self, env_config: ConfigEnvironment) -> None:
        super(DefaultScenario, self).inject_config(env_config)
        # Initialize TileDB storage needed for the scenario specific data
        if self._env_config is not None and self._env_config.temp_dir is not None:
            abs_path = pathlib.Path(self._env_config.temp_dir).resolve().joinpath('def_tdb_arrays')
            self.__tiledb_group_name = abs_path.as_uri()
            self.__tiledb_stats_array = abs_path.joinpath('stats').as_uri()
        # Create the tileDB group of arrays used by this scenario
        if not tiledb.array_exists(self.__tiledb_group_name):
            tiledb.group_create(self.__tiledb_group_name)

    def get_benchmark_workload(self) -> str:
        return self._default_workload

    def get_data_generator_workload(self) -> str:
        return self._default_workload

    def get_fragmenter(self) -> PFragmenter:
        return DefaultFragmenter(self._num_fragments)

    def get_fragment_sorter(self) -> PFragmentSorter:
        return DefaultFragmentSorter()

    def get_agents(self) -> Tuple[ABCAgent]:
        return self._agents

    def get_fragment_instance_meta_fields(self) -> Union[Sequence, None]:
        return None

    def get_fragment_global_meta_fields(self) -> Union[Sequence, None]:
        return None

    def make_collective_step(self, actions: Dict[int, int]) -> None:
        for agent_id, action in actions.items():
            s_agent: ABCAgent = self._agents[agent_id]
            s_agent.do_action(action)

    def do_run_benchmark(self) -> bool:
        # TODO: Actual decision logic for running or not the benchmark
        return True

    def process_benchmark_stats(self, bench_end_result: BenchmarkStatus) -> None:
        if bench_end_result.failed or not bench_end_result.completed:
            raise RuntimeError("Scenario should receive a completed non failed benchmark only")

        if bench_end_result.result is None:
            raise TypeError("Benchmark results are empty")

        bench_stats = bench_end_result.result
        # TODO: Process throughput
        # TODO: Process per node stats
        nodes = self.get_ordered_nodes()
        fragments = self._actual_fragments
        result = np.zeros((len(nodes), len(fragments)), dtype=np.int32)

        for node in nodes:
            node_stats = [d for d in bench_stats.nodes if d['nodeId'] == node.node_id]
            if len(node_stats) == 0:
                continue
            node_idx = nodes.index(node)
            for stats in node_stats:
                frag_stats: Dict = stats['frags']
                for k, v in frag_stats.items():
                    frag_idx = fragments.index(k)
                    frag: Dict = v
                    result[node_idx, frag_idx] = result[node_idx, frag_idx] + frag.get('r', 0)

        self._write_stats(result)

    def get_reward(self) -> Dict[int, float]:
        # TODO: Reward function
        pass

    def get_observation(self, registry_read: PRegistryReadClient) -> Dict[int, np.ndarray]:
        agent_nodes, fragments, obs = self.get_full_allocation_observation(registry_read=registry_read)
        # Expand observation space per agent to include read frequencies from the latest benchmark run or with
        # default values if no benchmark values yet available
        self._initialize_stat_values_store_if_needed(obs.shape)
        stats = self._retrieve_stats()
        obs = obs[..., np.newaxis]
        obs = np.insert(obs, 1, stats, axis=2)
        # Feed to agents for "filtering"
        obs_n: Dict[int, np.ndarray] = {}
        for agent in self.get_agents():
            obs_n[agent.id] = agent.filter_observation(agent_nodes, obs)
        return obs_n

    def flush(self) -> None:
        self._clear_arrays()

    def _initialize_stat_values_store_if_needed(self, shape: Tuple[int, ...]) -> None:
        """Initialize storage for the benchmark statistics if it wasn't created yet"""

        if tiledb.array_exists(self.__tiledb_stats_array):
            return
        # Create array with one dense dimension to store read statistics from the latest benchmark run.
        dom = tiledb.Domain(tiledb.Dim(name='n', domain=(0, shape[0]-1), tile=shape[0]-1, dtype=np.int64),
                            tiledb.Dim(name='f', domain=(0, shape[1]-1), tile=(shape[1]-1)//4, dtype=np.int64))
        # Schema contains one attribute for READ count
        schema = tiledb.ArraySchema(domain=dom, sparse=False, attrs=[tiledb.Attr(name='read', dtype=np.float32)])
        # Create the (empty) array on disk.
        tiledb.DenseArray.create(self.__tiledb_stats_array, schema)
        # Fill with zeroes
        with tiledb.DenseArray(self.__tiledb_stats_array, mode='w') as rr:
            zero_data = np.zeros(shape)
            rr[:] = zero_data

    def _clear_arrays(self) -> None:
        """Clear out local stat values"""
        if tiledb.array_exists(self.__tiledb_group_name):
            tiledb.remove(self.__tiledb_group_name)

    def _retrieve_stats(self) -> np.ndarray:
        """Get read values stored locally"""
        with tiledb.DenseArray(self.__tiledb_stats_array, mode='r') as rr:
            return rr[:]['read']

    def _write_stats(self, stats: np.ndarray) -> None:
        """
        Write benchmark stats to the local storage
        :param stats: Expected array must have shape (num of nodes, num of fragments, 1)
        """
        with tiledb.DenseArray(self.__tiledb_stats_array, mode='w') as rr:
            rr[:] = stats
