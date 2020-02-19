from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.scenario.abc import ABCScenario

import numpy as np
import logging


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
        :param scenario: Scenario object implementing ABCScenario.
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

        self._scenario = scenario
        self._registry_ctrl = registry_ctrl
        self._registry_write = registry_write
        self._registry_read = registry_read

    def initialize_registry(self):
        """
        Initialize or re-initialize the registry. All existing data will be removed, all data generated anew.
        """
        # Flush existing data
        flush_result = self._registry_ctrl.ctrl_flush()
        if flush_result.failed:
            raise RuntimeError(flush_result.error)
        # Re-initialize nodes
        nodes_populate_res = self._registry_ctrl.ctrl_populate_default_nodes()
        if nodes_populate_res.failed:
            raise RuntimeError(nodes_populate_res.error)
        # Registry must be in the 'benchmark' mode, meaning not accepting any modification (write) commands
        set_bench_result = self._registry_ctrl.ctrl_set_benchmark_mode()
        if set_bench_result.failed:
            raise RuntimeError(set_bench_result.error)
        # Generate data
        data_gen_result = self._registry_ctrl.ctrl_generate_data(self._scenario.get_datagenerator_workload())
        if data_gen_result.failed:
            raise RuntimeError(data_gen_result.error)
        if not data_gen_result.result:
            raise RuntimeError("Data was not generated, check the registry logs for more information")


    def reset(self):
        obs_n = []
        agents = self._scenario.get_agents()
        for agent in agents:
            obs_n.append(agent.get_observation())
        return obs_n

    def step(self, action_n):
        obs_n = []
        reward_n = []
        done_n = []
        info_n = {'n': []}

        agents = self._scenario.get_agents()
        for i, agent in enumerate(agents):
            self._set_action(action_n[i], agent, self.action_space[i])


        return obs_n, reward_n, done_n, info_n
