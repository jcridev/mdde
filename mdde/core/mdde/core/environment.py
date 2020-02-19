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
        :param scenario: Scenario object implementing ABCScenario. If any initialization of the scenario is required,
        such as generating data, definition of the agents and their actions, etc., it must be done prior passing the
        object to the constructor
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
        self.registry_ctrl = registry_ctrl
        self.registry_write = registry_write
        self.registry_read = registry_read

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
