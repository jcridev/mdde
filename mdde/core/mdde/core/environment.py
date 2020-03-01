import logging
from typing import Set, Tuple, Dict

import numpy as np

from mdde.core.exception import EnvironmentInitializationError
from mdde.registry.container import RegistryResponseHelper
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.scenario.abc import ABCScenario
from mdde.registry.enums import ERegistryMode


class Environment:
    """
    Entry point to MDDE. Reinforcement learning frameworks should be wrapped around this class to function
    """

    def __init__(self,
                 scenario: ABCScenario,
                 registry_ctrl: PRegistryControlClient,
                 registry_write: PRegistryWriteClient,
                 registry_read: PRegistryReadClient):
        """
        Environment constructor
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

        self._scenario = scenario
        self._registry_ctrl = registry_ctrl
        self._registry_write = registry_write
        self._registry_read = registry_read

        self.activate_scenario()

    def activate_scenario(self):
        """
        Verify the current scenario and ensure it's basic correctness before the start of experiments
        """
        # Make sure the same data node isn't assigned to more than one agent at a time
        nodes_per_agent: [Set[str]] = []
        for agent in self._scenario.get_agents():
            nodes_per_agent.append(set(agent.get_data_node_ids))
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
        fragmentation_requires_shuffle = fragmenter.run_fragmentation(self._registry_read, self._registry_write)
        self._logger.info("Finished fragmentation, shuffle required: %r", fragmentation_requires_shuffle)
        # Switch to shuffle
        self._set_registry_mode(ERegistryMode.shuffle)
        # Shuffle tuples if fragmentation introduced any changes in the registry
        if fragmentation_requires_shuffle:
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
        # Call reset
        self._set_registry_mode(ERegistryMode.benchmark)
        reset_call_response = self._registry_ctrl.ctrl_reset()
        RegistryResponseHelper.raise_on_error(reset_call_response)
        # Retrieve the observations
        return self.observation_space

    debug_reward: float = 0.0  # TODO: Replace with the actual reward function

    def step(self, action_n: Dict[int, int])\
            -> Tuple[Dict[int, np.ndarray], Dict[int, float]]:
        """
        Execute actions chosen for each agent, get resulted rewards and new observations
        :param action_n: Dict['agent_id':action_id]
        :return: Dict['agent_id':np.ndarray], Dict['agent_id':float]
        """
        # TODO: Return reward per agent
        # execute actions
        # TODO: Execute actions logic in the scenario

        self._scenario.make_collective_step(action_n)

        obs_n = self.observation_space
        reward_n = {}

        for agent in self._scenario.get_agents():
            reward_n[agent.id] = self.debug_reward

        self.debug_reward += 1.0  # TODO: Remove later

        return obs_n, reward_n

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
            RegistryResponseHelper.raise_on_error(set_bench_result)
