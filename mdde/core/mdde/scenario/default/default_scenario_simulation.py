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
                 throughput_history_relevance: int = 25,
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
                         write_stats=write_stats,
                         ignore_conflicting_actions=ignore_conflicting_actions)

        self._benchmark_mode = EBenchmark.COUNTERFEIT
        self._fresh_start: bool = False
        """Flag for the fresh start of the scenario, set to false after the real benchmark is executed first time."""

        #self._throughput_history_len = throughput_history_relevance
        """Length of the throughput history. Benchmark history is not used in the reward calculation until it's filled
        (all values are non-zero)."""
        #self._throughput_history = collections.deque(maxlen=self._throughput_history_len)
        """Array  storing history for the last 10 benchmark runs."""

    def do_run_benchmark(self) -> EBenchmark:
        """
        Decide if a benchmark run should be executed.
        Benchmark should be executed at the specified step frequency and only when agents are performing the amount of
        valid steps above the specified threshold.
        :return: EBenchmark.
        """
        if self._fresh_start:
            self._fresh_start = False
            return EBenchmark.DEFAULT

        return super().do_run_benchmark()
