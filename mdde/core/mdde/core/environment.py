import logging
import time
from typing import Set, Tuple, Dict, Union

import numpy as np

from mdde.agent.abc import ABCAgent
from mdde.config import ConfigEnvironment
from mdde.core.exception import EnvironmentInitializationError
from mdde.registry.container import BenchmarkStatus
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.scenario.abc import ABCScenario
from mdde.registry.enums import ERegistryMode
from mdde.util import assert_with_log


class Environment:
    """Entry point to MDDE. Reinforcement learning frameworks should be wrapped around this class to function"""

    def __init__(self,
                 config: Union[None, ConfigEnvironment],
                 scenario: ABCScenario,
                 registry_ctrl: PRegistryControlClient,
                 registry_write: PRegistryWriteClient,
                 registry_read: PRegistryReadClient):
        """
        Environment constructor
        :param config: (optional) MDDE configuration object
        :param scenario: Scenario object implementing ABCScenario
        :param registry_ctrl: Control commands for the MDDE registry implementation
        :param registry_write: Write commands for the MDDE registry implementation
        :param registry_read: Read commands for the MDDE registry implementation
        """
        if not isinstance(scenario, ABCScenario):
            raise TypeError("scenario must extend ABCScenario")

        if registry_ctrl is None:
            raise TypeError("registry control client can't be None")
        if registry_write is None:
            raise TypeError("registry write client can't be None")
        if registry_read is None:
            raise TypeError("registry read client can't be None")

        if scenario is None:
            raise TypeError("scenario can't be None")

        self._logger = logging.getLogger('Environment')
        """Environment instance specific logger"""

        self._scenario: ABCScenario = scenario
        """Scenario executed within the current environment"""
        self._registry_ctrl: PRegistryControlClient = registry_ctrl
        """Control commands for the MDDE registry implementation"""
        self._registry_write: PRegistryWriteClient = registry_write
        """Write commands for the MDDE registry implementation"""
        self._registry_read: PRegistryReadClient = registry_read
        """Read commands for the MDDE registry implementation"""

        self._config: Union[None, ConfigEnvironment] = config
        """Environment config file, if required. Injected into the scenario"""

        self.activate_scenario()

    def activate_scenario(self):
        """
        Verify the current scenario and ensure it's basic correctness before the start of experiments
        """
        self._scenario.inject_config(self._config)
        # Make sure the same data node isn't assigned to more than one agent at a time
        nodes_per_agent: [Set[str]] = []
        for agent in self._scenario.get_agents():
            nodes_per_agent.append(set(agent.data_node_ids))
        if len(set.intersection(*nodes_per_agent)) > 0:
            raise ValueError("The same data node id can't be assigned to more than one agent at the time")
        # Attach registry clients to the agents
        for agent in self._scenario.get_agents():
            agent.attach_registry(self._registry_read, self._registry_write)

    def initialize_registry(self):
        """
        Initialize or re-initialize the registry. All existing data will be removed, all data generated anew.
        """
        self._logger.info("Environment initialization starting")
        # Registry must be in the 'shuffle' mode
        self._set_registry_mode(ERegistryMode.shuffle)
        # Flush existing data
        self._logger.info("Flushing current environment data")
        flush_result = self._registry_ctrl.ctrl_flush()
        if flush_result.failed:
            raise EnvironmentInitializationError(flush_result.error)
        # Re-initialize nodes
        self._logger.info("Initializing nodes from the registry config")
        nodes_populate_res = self._registry_ctrl.ctrl_populate_default_nodes()
        if nodes_populate_res.failed:
            raise EnvironmentInitializationError(nodes_populate_res.error)
        # Registry must be in the 'benchmark' mode, meaning not accepting any modification (write) commands
        self._set_registry_mode(ERegistryMode.benchmark)
        # Generate data
        self._logger.info("Executing workload to generate data")
        data_gen_result = self._registry_ctrl.ctrl_generate_data(self._scenario.get_data_generator_workload())
        if data_gen_result.failed:
            err = EnvironmentInitializationError(data_gen_result.error)
            self._logger.critical(err)
            raise err
        if not data_gen_result.result:
            err = EnvironmentInitializationError("Initial data was not generated, "
                                                 "check the registry logs for more information")
            self._logger.critical(err)
            raise err
        # Run initial fragmentation
        self._logger.info("Fragment generation starting")
        fragmenter = self._scenario.get_fragmenter()
        requires_shuffle, fragments_ids = fragmenter.run_fragmentation(self._registry_read, self._registry_write)
        self._scenario.inject_fragments(fragments_ids)
        self._logger.info("Finished fragmentation, shuffle required: %r", requires_shuffle)
        # Switch to shuffle
        self._set_registry_mode(ERegistryMode.shuffle)
        # Shuffle tuples if fragmentation introduced any changes in the registry
        if requires_shuffle:
            registry_to_data_sync_result = self._registry_ctrl.ctrl_sync_registry_to_data()
            if registry_to_data_sync_result.failed:
                err = EnvironmentInitializationError(data_gen_result.error)
                self._logger.critical(err)
                raise err
        # Create an initial default snapshot (Environment will roll back to this snapshot at reset)
        self._logger.info("Creating the initial default snapshot")
        snapshot_create_result = self._registry_ctrl.ctrl_snapshot_create(True)
        if snapshot_create_result.failed:
            err = EnvironmentInitializationError(snapshot_create_result.error)
            self._logger.critical(err)
            raise err
        default_snapshot_id = snapshot_create_result.result
        if default_snapshot_id:
            self._logger.info("Default snapshot created with ID: %s", default_snapshot_id)
        else:
            err = EnvironmentInitializationError("Failed to create a new default snapshot, "
                                                 "no ID returned from the registry")
            self._logger.critical(err)
            raise err
        # Initialize the action space
        self._logger.info("Initializing action space per agent")
        self._initialize_action_space()
        self._logger.info("Environment initialization is complete")

    def reset(self) -> Dict[int, np.ndarray]:
        self._logger.info("Resetting the environment")
        # Call registry reset
        self._set_registry_mode(ERegistryMode.benchmark)
        reset_call_response = self._registry_ctrl.ctrl_reset()
        if reset_call_response.failed:
            raise RuntimeError(reset_call_response.error)
        # Reset agents state
        self._scenario.reset()
        # Retrieve the observations
        return self.observation_space

    def step(self, action_n: Dict[int, int]) \
            -> Tuple[Dict[int, np.ndarray], Dict[int, float], Dict[int, bool]]:
        """
        Execute actions chosen for each agent, get resulted rewards and new observations
        :param action_n: Dict['agent_id':action_id]
        :return: Dict['agent_id':'np.ndarray of observations'], Dict['agent_id':'reward'], Dict['agent_id':'done flag']
        """
        # Act
        self._scenario.make_collective_step(action_n)
        # Measure
        if self._scenario.do_run_benchmark():
            # Run the benchmark now if appropriate for the current scenario
            self.benchmark()
        # Observe
        obs_n = self.observation_space
        # Get the reward
        reward_n = self._scenario.get_reward()
        # Get the done flag
        done_n = {a.id: a.done for a in self._scenario.get_agents()}

        assert_with_log(obs_n is not None, "Unable to retrieve observations", self._logger)
        assert_with_log(reward_n is not None, "Unable to retrieve rewards", self._logger)

        return obs_n, reward_n, done_n

    @property
    def agents(self) -> Union[None, Tuple[ABCAgent]]:
        """
        If the scenario is set, return the agents defined in the active scenario
        :return: Tuple of agents
        """
        if self._scenario is None:
            return None
        return self._scenario.get_agents()

    @property
    def observation_space(self) -> Dict[int, np.ndarray]:
        """
        Retrieve all observation spaces for all agents
        :return: Dict['agent_id':np.ndarray]
        """
        return self._scenario.get_observation(registry_read=self._registry_read)

    @property
    def action_space(self) -> Dict[int, int]:
        """
        Retrieve the action space per agent.
        :return: Dict['agent_id', number_of_discrete_actions]
        """
        act_n: Dict[int, int] = {}
        for agent in self._scenario.get_agents():
            act_n[agent.id] = agent.get_actions()
        return act_n

    def benchmark(self, without_waiting: bool = False) -> Union[None, BenchmarkStatus]:
        """Execute benchmark run"""
        # Execute the shuffle queue
        self._set_registry_mode(target_mode=ERegistryMode.shuffle)
        self._logger.info("Executing the shuffle queue")
        sync_data_result = self._registry_ctrl.ctrl_sync_registry_to_data()
        if sync_data_result.failed:
            raise RuntimeError(sync_data_result.error)
        s_queue_res_msg = "Shuffle queue executed: {}".format(sync_data_result.result)
        if not sync_data_result.result:
            self._logger.warning(s_queue_res_msg)
        else:
            self._logger.info(s_queue_res_msg)
        # Execute the benchmark
        self._set_registry_mode(target_mode=ERegistryMode.benchmark)
        self._logger.info("Executing the benchmark")
        bench_start_result = self._registry_ctrl.ctrl_start_benchmark(
            workload_id=self._scenario.get_benchmark_workload())
        if bench_start_result.failed:
            raise RuntimeError(bench_start_result.error)
        if without_waiting:
            return None  # Don't wait for the benchmark to end
        while True:
            time.sleep(15)
            bench_status_response = self.benchmark_status()
            if bench_status_response.completed or bench_start_result.failed:
                break
        # Switch back to shuffle
        self._set_registry_mode(target_mode=ERegistryMode.shuffle)
        # Return the result
        self._scenario.process_benchmark_stats(bench_status_response)
        return bench_status_response

    def benchmark_status(self) -> BenchmarkStatus:
        bench_status = self._registry_ctrl.ctrl_get_benchmark()
        if bench_status.failed:
            raise RuntimeError(bench_status.error)
        return bench_status.result

    def _initialize_action_space(self):
        """
        Initialize actions for agents
        """
        agent_nodes, fragments, obs = self._scenario.get_full_allocation_observation(registry_read=self._registry_read)
        for agent in self._scenario.get_agents():
            agent.create_action_space(agent_nodes, fragments, obs)
            self._logger.info("Agent '{}' initialized with the action space size: {}."
                              .format(agent.id, agent.get_actions()))

    def _set_registry_mode(self, target_mode: ERegistryMode):
        """
        Switch current registry mode to a target mode if needed (if it's not already in that specific mode of execution)
        :param target_mode: ERegistryMode value
        """
        get_mode_result = self._registry_ctrl.ctrl_get_mode()  # Verify that the environment is in benchmark mode
        if get_mode_result.failed:
            raise EnvironmentInitializationError(get_mode_result.error)
        if get_mode_result.result == ERegistryMode.unknown:
            raise RuntimeError("Registry is in unknown mode")
        if get_mode_result.result != target_mode:
            self._logger.info("Switching registry to %s mode, current mode: %s",
                              target_mode.name, get_mode_result.result.name)
            if target_mode == ERegistryMode.benchmark:
                set_bench_result = self._registry_ctrl.ctrl_set_benchmark_mode()
            elif target_mode == ERegistryMode.shuffle:
                set_bench_result = self._registry_ctrl.ctrl_set_shuffle_mode()
            else:
                raise RuntimeError("Illegal registry mode switch attempt")
            if set_bench_result.failed:
                raise RuntimeError(set_bench_result.error)
