import logging
import pathlib
import csv
import collections

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
from mdde.scenario.abc import EBenchmark
from mdde.scenario.default import DefaultScenario


class DefaultScenarioSimulation(DefaultScenario):
    """
    Full observation space multi-agent data distribution scenario with read-only workload.
    Similar to the DefaultScenario but oriented on working with the predictable COUNTERFEIT benchmark.
    Suitable for quick local hypothesis verification and reward function testing.
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
                 throughput_history_relevance: int = 25):
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
        :param throughput_history_relevance: (optional) How far back throughput history should be taken into account
        when calculating the reward.
        """
        super().__init__(num_fragments=num_fragments,
                         num_steps_before_bench=num_steps_before_bench,
                         agents=agents,
                         benchmark_clients=benchmark_clients,
                         data_gen_workload=data_gen_workload,
                         bench_workload=bench_workload,
                         corr_act_threshold=corr_act_threshold,
                         write_stats=write_stats)

        self._benchmark_mode = EBenchmark.COUNTERFEIT

        self._throughput_history_len = throughput_history_relevance
        """Length of the throughput history. Benchmark history is not used in the reward calculation until it's filled
        (all values are non-zero)."""
        self._throughput_history = collections.deque(maxlen=self._throughput_history_len)
        """Array  storing history for the last 10 benchmark runs."""

        self._storage_multiplier: float = 0.0
        """Emphasis on storage related term in the reward function."""

    def _reward_bench(self, contents_n):
        """Calculate the reward based on the statistics collected during the latest benchmark run execution"""
        reward_n = {}  # agent_id : float reward
        current_throughput = self._throughput  # latest throughput value, same for all agents
        if current_throughput is not None:
            self._throughput_history.append(current_throughput)

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
                if a_act[0] == 1 and a_act[1] == EActionResult.ok.value:
                    # Only give rewards for the actions that were success and not "do nothing" or "done"
                    agent_correctness_multiplier += 1.0 / self._num_steps_before_bench
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
                                     + (current_throughput / contents_n[agent_obj.id] * self._storage_multiplier)
        if self._dump_stats:
            self._dump_scenario_stats_at_bench(step_idx=self._step_count,
                                               agent_read_n=stat_agent_reads,
                                               agent_corr_n=stat_agent_correct,
                                               throughput=current_throughput,
                                               total_reads=total_reads)
        return reward_n
