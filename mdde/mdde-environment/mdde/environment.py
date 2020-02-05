from mdde.registry import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.scenario import ABCScenario
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
        raise NotImplementedError

    def step(self):
        raise NotImplementedError
