import logging
import pathlib
import csv

from typing import Tuple, Union, Sequence, Dict, List
from pathlib import Path

import numpy as np
import tiledb
from tiledb.libtiledb import TileDBError

from mdde.agent.abc import ABCAgent
from mdde.agent.enums import EActionResult
from mdde.config import ConfigEnvironment
from mdde.fragmentation.default import DefaultFragmenter, DefaultFragmentSorter
from mdde.fragmentation.protocol import PFragmentSorter, PFragmenter
from mdde.registry.container import BenchmarkStatus
from mdde.registry.protocol import PRegistryReadClient
from mdde.registry.workload import EDefaultYCSBWorkload, YCSBWorkloadInfo
from mdde.scenario.abc import ABCScenario, EBenchmark


class DefaultScenario(ABCScenario):
    """
    Full observation space multi-agent data distribution scenario with read-only workload
    """

    def __init__(self,
                 num_fragments: int,
                 num_steps_before_bench: int,
                 agents: Sequence[ABCAgent],
                 benchmark_clients: int = 5,
                 data_gen_workload: EDefaultYCSBWorkload = EDefaultYCSBWorkload.READ_10000_100000_LATEST,
                 bench_workload: EDefaultYCSBWorkload = EDefaultYCSBWorkload.READ_10000_100000_LATEST,
                 corr_act_threshold: float = 0.7,
                 write_stats: bool = False,
                 ignore_conflicting_actions: bool = False):
        """
        Constructor of the default scenario.
        :param num_fragments: Target number of fragments to be generated out of data record present in data nodes
        :param num_steps_before_bench: Number of joint steps taken by the agents before benchmark is executed
        :param agents: Collection of configured agents
        :param benchmark_clients: Number of the benchmark clients to be created during the benchmark run.
        Default value is 1. Values ∈ (0, inf).
        :param data_gen_workload: (optional) Workload ID which should be used for data generation.
        :param bench_workload: (optional) Workload ID which should be used for benchmarking.
        :param corr_act_threshold: (optional) Threshold of the correct action percentage taken by the agents within
        the number of steps between benchmarks. If the percentage is lower than the threshold, benchmark is not
        executed. Values ∈ [0., 1.].
        """
        super().__init__('Default scenario')
        self._logger = logging.getLogger('Default scenario')

        self.__workload_data_gen_info: YCSBWorkloadInfo = data_gen_workload.value
        """Data generation workload used for the scenario"""
        self.__workload_bench_info: YCSBWorkloadInfo = bench_workload.value
        """Benchmark workload used for the scenario"""

        self._benchmark_clients = benchmark_clients
        """Number of YCSB clients per benchmark run"""

        self._TOTAL_READS = self.__workload_bench_info.operation_count
        """Total number of read operations per benchmark run"""

        self._num_fragments: int = num_fragments
        """Target number of fragments to form from the generated data"""
        self._agents: Tuple[ABCAgent, ...] = tuple(agents)
        """Pool of agents"""

        if num_steps_before_bench < 1:
            raise ValueError("num_steps_before_bench must be > 0")

        self._num_steps_before_bench = num_steps_before_bench
        """Number of steps executed without running benchmark."""
        self._current_step = 0
        """Step counter, incremented at every self.do_run_benchmark until reaches self._num_steps_before_bench - 1"""
        self._action_history = np.zeros((self._num_steps_before_bench, len(self._agents), 2), dtype=np.int16)
        """Array used as a ring buffer for the step results history per agent. 
        self._action_history[0] - first step after the benchmark, array of all agents.
        self._action_history[0][0] - first step result for the first agent.
        self._action_history[0][0][0] - 1 - agent took action during the step, 0 - no action was taken
        self._action_history[0][0][1] - EActionResult value
        """
        self._action_history.fill(-1)

        if corr_act_threshold < 0 or corr_act_threshold > 1.:
            raise ValueError("corr_act_threshold must be a value in range [0., 1.].")

        self._corr_act_threshold = corr_act_threshold
        """Threshold of the correct action percentage taken by the agents within the number of steps between benchmark 
        runs."""

        self.__tiledb_group_name: str = 'def_tdb_arrays'
        """Tile DB group of arrays where all scenario arrays are located"""
        self.__tiledb_stats_array: Union[None, str] = None
        """Array of statistics received from the previous benchmark run"""
        self._throughput: Union[float, None] = None
        """Raw throughput value received from the previous benchmark run"""
        self.__selected_actions: Union[None, np.ndarray] = None

        self._benchmark_data_ready: bool = False
        """Flag: Set to true after the benchmark run was executed and the results passed to the scenario"""

        self.__file_csv_writer = None
        self._mdde_result_folder_root: Union[None, str] = None
        """Folder where all of the statistical data should be dumped."""

        self._dump_stats = write_stats
        """True - write additional statistics to the results folder."""
        self._step_count = 0

        self._benchmark_mode = EBenchmark.DEFAULT
        """Default benchmark mode"""

        self._storage_multiplier: float = 0.0
        """Emphasis on storage related term in the reward function."""
        self._do_nothing_worth: float = 0.0
        """Default (non-bench) reward for doing nothing"""

        self._ignore_conflicting_actions: bool = ignore_conflicting_actions
        """If agents take legal actions, but fail due to conflicts, still give the agents a proper reward"""
        self._latest_legal_actions: Union[None, Dict[int, np.ndarray]] = None
        """Legal actions as returned to the agents with previously generated observations per agent"""

    def set_storage_importance(self, importance: float) -> None:
        """Adjust the multiplier for the storage importance > 0 (Recommended range [0.0, 1.0])"""

        if importance < 0:
            raise ValueError("Importance of the storage can't be negative. ")

        self._storage_multiplier = importance

    def set_do_nothing_worth(self, worth: float) -> None:
        """Adjust the importance of do-nothing value (Recommended range [0.0, 1.0])"""
        if not isinstance(worth, float):
            raise TypeError("Do-nothing worth must be a floating number.")
        if worth < 0.0:
            raise ValueError("Importance of the storage can't be negative. ")

        self._do_nothing_worth: float = worth

    def __del__(self):
        self._logger.info('Shutting down')
        if self.__file_csv_writer:
            # Close a CSV writer if there's one
            self._logger.info('Closing CSV writer')
            file = self.__file_csv_writer[0]
            file.close()

    def inject_config(self, env_config: ConfigEnvironment) -> None:
        super(DefaultScenario, self).inject_config(env_config)
        # Initialize TileDB storage needed for the scenario specific data
        exp_tmp_dir = self._env_config.temp_dir(experiment_id=self.experiment_id)
        if self._env_config is not None and exp_tmp_dir is not None:
            abs_path = pathlib.Path(exp_tmp_dir).resolve().joinpath('def_tdb_arrays')
            self.__tiledb_group_name = abs_path.as_uri()
            self.__tiledb_stats_array = abs_path.joinpath('stats').as_uri()
        # Create the tileDB group of arrays used by this scenario
        tdb_gtype = tiledb.object_type(self.__tiledb_group_name)
        if tdb_gtype is None:  # Group does not exist
            tiledb.group_create(self.__tiledb_group_name)
        elif tdb_gtype == 'array':  # Exist but an array
            tiledb.remove(self.__tiledb_group_name)  # Remove the array
            tiledb.group_create(self.__tiledb_group_name)  # Create a group instead
        self._clear_arrays()

        self._mdde_result_folder_root = env_config.result_dir(self, get_root=True)

    def get_benchmark_workload(self) -> str:
        return self.__workload_data_gen_info.tag

    def get_data_generator_workload(self) -> str:
        return self.__workload_bench_info.tag

    def get_benchmark_num_clients(self) -> int:
        return self._benchmark_clients

    def get_fragmenter(self) -> PFragmenter:
        return DefaultFragmenter(self._num_fragments)

    def get_fragment_sorter(self) -> PFragmentSorter:
        return DefaultFragmentSorter()

    def get_agents(self) -> Tuple[ABCAgent]:
        return self._agents

    def get_fragment_instance_meta_fields(self) -> Union[Sequence[str], None]:
        return None

    def get_fragment_global_meta_fields(self) -> Union[Sequence[str], None]:
        return None

    def make_collective_step(self, actions: Dict[int, int]) -> None:
        self._step_count += 1
        # Default values for the initial step in the row
        if self._current_step == 0:
            self._action_history.fill(-1)
        # Execute actions
        step_action_res = np.zeros((len(self._agents), 2), dtype=np.int16)
        step_action_res.fill(-1)
        ordered_a_ids = list(actions.keys())
        ordered_a_ids.sort()
        for agent_id in ordered_a_ids:
            action = actions[agent_id]
            s_agent: ABCAgent = self._agents[agent_id]
            aa_res: EActionResult = s_agent.do_action(action)

            if aa_res == EActionResult.denied and self._ignore_conflicting_actions:
                if self._latest_legal_actions is not None:
                    agent_legal_act = self._latest_legal_actions[agent_id]
                    if agent_legal_act[action] == 1:
                        # If the action was previously declared legal, still ok it.
                        aa_res = EActionResult.ok

            aa_val = np.full(2, [1, aa_res.value], dtype=np.int16)
            step_action_res[agent_id] = aa_val
        self._action_history[self._current_step] = step_action_res

    def do_run_benchmark(self) -> EBenchmark:
        """
        Decide if a benchmark run should be executed.
        Benchmark should be executed at the specified step frequency and only when agents are performing the amount of
        valid steps above the specified threshold.
        :return: EBenchmark.
        """
        if self._benchmark_data_ready:
            # Don't run benchmark multiple times in a row if the previous results were not processed
            return EBenchmark.NO_BENCHMARK
        if self._current_step == self._num_steps_before_bench - 1:
            self._current_step = 0
            # Check the quality of the steps
            # TODO: Refactor (it's ok for a small number of agents and steps but won't scale with massive experiments)
            agent_fails = np.zeros((len(self.get_agents()), 2), dtype=np.uint16)
            for agent_idx in range(0, len(self.get_agents())):
                for a_act_history_step in range(0, self._num_steps_before_bench):
                    a_act = self._action_history[a_act_history_step][agent_idx]
                    if a_act[0] == 1:  # increment the number of steps taken by the agent within the window
                        agent_fails[agent_idx][0] += 1
                        if a_act[1] == EActionResult.denied.value:  # increment failed steps
                            agent_fails[agent_idx][1] += 1
            a_act_fail = np.sum(agent_fails, 0)
            if a_act_fail[0] == 0:
                # None of the agents participated
                return EBenchmark.NO_BENCHMARK

            if a_act_fail[1] == 0:
                # All actions were correct
                return self._benchmark_mode

            # If agents participated (even if did nothing) compare to the threshold
            if a_act_fail[1] / a_act_fail[0] + self._corr_act_threshold <= 1:
                return self._benchmark_mode
            else:
                return EBenchmark.NO_BENCHMARK
        self._current_step += 1
        return EBenchmark.NO_BENCHMARK

    def process_benchmark_stats(self, registry_read: PRegistryReadClient, bench_end_result: BenchmarkStatus) -> None:
        if bench_end_result.failed or not bench_end_result.completed:
            raise RuntimeError("Scenario should receive a completed non failed benchmark only")

        if bench_end_result.result is None:
            raise TypeError("Benchmark results are empty")

        bench_stats = bench_end_result.result

        nodes = self.get_ordered_nodes()
        fragments = self._actual_fragments
        result_frags_total = np.zeros((len(fragments)), dtype=np.int32)
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
                    result_frags_total[frag_idx] = result_frags_total[frag_idx] + frag.get('r', 0)
                    result[node_idx, frag_idx] = result[node_idx, frag_idx] + frag.get('r', 0)
        self._logger.debug("Benchmark reads per fragments total: {}".format(result_frags_total))
        if self._logger.getEffectiveLevel() == logging.DEBUG:
            for d_node_idx, d_node_r in enumerate(result):
                self._logger.debug("Benchmark reads for node {}: {}".format(d_node_idx, d_node_r))

        self._initialize_stat_values_store_if_needed_read_obs_space(registry_read=registry_read)
        self._write_stats(result)
        self._throughput = bench_stats.throughput
        self._benchmark_data_ready = True

    def get_reward(self, reg_read: PRegistryReadClient) -> Dict[int, float]:
        if self._benchmark_data_ready:
            nodes, sorted_fragments, obs_full = self.get_full_allocation_observation(registry_read=reg_read)
            num_frags_per_agent = {a.id: 0 for a in self._agents}

            for node in nodes:
                a_id = node.agent_id
                n_idx = nodes.index(node)
                num_frags_per_agent[a_id] = num_frags_per_agent[a_id] + np.sum(obs_full[n_idx])

            # Return the reward taking in account the latest benchmark run
            self._benchmark_data_ready = False
            return self._reward_bench(num_frags_per_agent, len(sorted_fragments))
        else:
            # In between benchmark runs, return  pseudo-reward that indicates only the correctness of the action
            return self._reward_no_bench()

    def _reward_bench(self, contents_n, num_fragments):
        """Calculate the reward based on the statistics collected during the latest benchmark run execution"""
        reward_n = {}  # agent_id : float reward
        current_throughput = self._throughput  # latest throughput value, same for all agents

        nodes = self.get_ordered_nodes()
        stats = self._retrieve_stats()
        total_reads = np.sum(stats)
        stat_agent_reads = {}
        stat_agent_correct = {}
        for agent_idx in range(0, len(self.get_agents())):
            # Calculate individual reward for each agent based on the throughput and results of actions taken
            agent_obj = self.get_agents()[agent_idx]
            agent_node_idx = []
            for node_idx in range(0, len(nodes)):
                if nodes[node_idx].agent_id == agent_obj.id:
                    agent_node_idx.append(node_idx)
            # Summarize all reads for the run for all of the nodes mapped to the agent
            agent_reads = 0
            for a_node_idx in agent_node_idx:
                a_node_stats = stats[a_node_idx]
                agent_reads = np.sum(a_node_stats)
            # Get the summarized result of actions correctness
            agent_correctness_multiplier = 0.0
            for a_act_history_step in range(0, self._num_steps_before_bench):
                a_act = self._action_history[a_act_history_step][agent_idx]
                # Increase the multiplier for each correct action taken, if no correct actions were taken,
                # the final agent reward will be 0
                if a_act[0] == 1:
                    if a_act[1] == EActionResult.ok.value:
                        # Only give rewards for the actions that were success and not "done"
                        agent_correctness_multiplier += 1.0 / self._num_steps_before_bench
                    elif a_act[1] == EActionResult.did_nothing.value:
                        # Assign reward portion proportional to do nothing
                        agent_correctness_multiplier += self._do_nothing_worth / self._num_steps_before_bench
            # Agent reward

            self._logger.debug("Real reward: current_throughput={};agent_reads={};total_reads={};"
                               "agent_correctness_multiplier={}".format(current_throughput,
                                                                        agent_reads,
                                                                        total_reads,
                                                                        agent_correctness_multiplier))
            if self._dump_stats:
                stat_agent_reads[agent_obj.id] = agent_reads
                stat_agent_correct[agent_obj.id] = agent_correctness_multiplier

            reward_n[agent_obj.id] = current_throughput * (agent_reads / total_reads) * agent_correctness_multiplier \
                                     + (current_throughput * ((num_fragments - contents_n[agent_obj.id]) / num_fragments)
                                        * self._storage_multiplier)
        if self._dump_stats:
            self._dump_scenario_stats_at_bench(step_idx=self._step_count,
                                               agent_read_n=stat_agent_reads,
                                               agent_corr_n=stat_agent_correct,
                                               throughput=current_throughput,
                                               total_reads=total_reads)
        return reward_n

    def _reward_no_bench(self):
        """Calculate current reward in between benchmark runs based on the allocation only"""
        step = self._action_history[self._current_step - 1]
        reward_n = {}
        i = 0
        agents = self.get_agents()
        while i < len(agents):
            aid = agents[i].id
            a_step = step[i]
            if a_step[0] != 1:
                reward_n[aid] = 0.
            else:
                if a_step[1] == EActionResult.denied.value:
                    # Negative reward for an incorrect action
                    reward_n[aid] = -1.
                elif a_step[1] == EActionResult.did_nothing.value:
                    # Zero reward for doing nothing
                    reward_n[aid] = self._do_nothing_worth
                else:
                    # 1.0 for doing something which is correct
                    reward_n[aid] = 1.
            i += 1
        return reward_n

    def _dump_scenario_stats_at_bench(self,
                                      step_idx: int,
                                      agent_read_n: Dict[int, int],
                                      agent_corr_n: Dict[int, float],
                                      total_reads: int,
                                      throughput: float) -> None:
        """
        Write statistics data to a CSV file.
        Opens a CSV writer that remains open for the duration of the run.
        :param step_idx: Current step.
        :param agent_read_n: {agent_id: reads since last benchmark}.
        :param agent_corr_n: {agent_correctnessness since last benchmark}.
        :param total_reads: Total number of reads since last benchmark.
        :param throughput: Throughput.
        """
        active_agents = self._agents
        if not self.__file_csv_writer:
            file_path = Path(self._mdde_result_folder_root).joinpath('default_scenario.csv')
            csv_file = open(file_path, 'w', newline='')
            csv_writer = csv.writer(csv_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
            self.__file_csv_writer = (csv_file, csv_writer)
            # Generate a header
            header = ['step']
            for agent in active_agents:
                header.append("agent_reads_{}".format(agent.id))
                header.append("correctness_{}".format(agent.id))
            header.append('total_reads')
            header.append('throughput')
            csv_writer.writerow(header)
        csv_writer = self.__file_csv_writer[1]
        row = [step_idx]
        for agent in active_agents:
            row.extend([agent_read_n.get(agent.id),
                        agent_corr_n.get(agent.id)])
        row.append(total_reads)
        row.append(throughput)
        csv_writer.writerow(row)
        # Force flush
        file = self.__file_csv_writer[0]
        file.flush()

    def get_observation(self, registry_read: PRegistryReadClient) \
            -> Tuple[Dict[int, np.ndarray], Dict[int, np.ndarray]]:
        agent_nodes, fragments, obs = self.get_full_allocation_observation(registry_read=registry_read)
        # Expand observation space per agent to include read frequencies from the latest benchmark run or with
        # default values if no benchmark values yet available
        self._initialize_stat_values_store_if_needed(obs.shape)
        stats = self._retrieve_stats()
        # Normalize stats
        normalizer = np.vectorize(lambda x: np.where(x > 0, x / self._TOTAL_READS, x))
        stats = normalizer(stats)
        # Create the final observation space shape
        obs = obs.astype(np.float64)
        obs = obs[..., np.newaxis]
        obs = np.insert(obs, 1, stats, axis=2)
        # Feed to agents for "filtering"
        obs_n: Dict[int, np.ndarray] = {}
        action_legal_n: Dict[int, np.ndarray] = {}
        for agent in self.get_agents():
            obs_n[agent.id], action_legal_n[agent.id] = agent.filter_observation(agent_nodes, fragments, obs)

        self._latest_legal_actions = action_legal_n
        return obs_n, action_legal_n

    def flush(self) -> None:
        # Delete arrays when finished
        self._clear_arrays()

    def reset(self) -> EBenchmark:
        # Reset agents
        super().reset()
        # Flush arrays, no point to store these between episodes
        self.flush()
        return self._benchmark_mode

    def _initialize_stat_values_store_if_needed_read_obs_space(self, registry_read: PRegistryReadClient) -> None:
        """
        Initialize the TileDB stats array with no pre-supplied observations shape. It will retrieve, observations, and
        create a new array based on that. Only use in cases where it's uncertain if the array was initialized or not but
        there is no observation space yet.
        :param registry_read: Read-only client for the registry.
        """
        if self.__tiledb_stats_array is not None and tiledb.array_exists(self.__tiledb_stats_array):
            return

        agent_nodes, fragments, obs = self.get_full_allocation_observation(registry_read=registry_read)
        # Expand observation space per agent to include read frequencies from the latest benchmark run or with
        # default values if no benchmark values yet available
        self._initialize_stat_values_store_if_needed(obs.shape)

    def _initialize_stat_values_store_if_needed(self, shape: Tuple[int, ...]) -> None:
        """
        Initialize storage for the benchmark statistics if it wasn't created yet.
        :param shape: Shape of the stats map.
        """

        if self.__tiledb_stats_array is not None and tiledb.array_exists(self.__tiledb_stats_array):
            return
        # Create array with one dense dimension to store read statistics from the latest benchmark run.
        dom = tiledb.Domain(tiledb.Dim(name='n', domain=(0, shape[0] - 1), tile=shape[0] - 1, dtype=np.int64),
                            tiledb.Dim(name='f', domain=(0, shape[1] - 1), tile=(shape[1] - 1), dtype=np.int64))
        # Schema contains one attribute for READ count
        schema = tiledb.ArraySchema(domain=dom, sparse=False, attrs=[tiledb.Attr(name='read', dtype=np.int32)])
        # Create the (empty) array on disk.
        tiledb.DenseArray.create(self.__tiledb_stats_array, schema)
        # Fill with zeroes
        with tiledb.DenseArray(self.__tiledb_stats_array, mode='w') as rr:
            zero_data = np.zeros(shape, dtype=np.int32)
            rr[:] = zero_data

    def _clear_arrays(self) -> None:
        """Clear out local stat values"""
        try:
            tiledb.ls(self.__tiledb_group_name, lambda tdb_obj, tdb_type: tiledb.remove(tdb_obj))
        except TileDBError:
            self._logger.debug("No TileDB group to clear out.")

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
