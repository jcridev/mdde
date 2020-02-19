from abc import ABC, abstractmethod
from typing import List

import numpy as np

from mdde.agent.abc import ABCAgent
from mdde.core.exception import MddeError
from mdde.fragmentation.protocol import PFragmenter, PFragmentSorter
from mdde.registry.protocol import PRegistryReadClient


class ABCScenario(ABC):
    """
    Subclass this class overriding provided abstract methods to define a new scenario for the environment
    """

    # TODO: Declaration of the agents
    #   TODO: mapping agents to data nodes
    #   TODO: actions definitions
    # TODO: Declaration of the meta values (global and local)
    # TODO: Declaration of the fragments generator
    # TODO: Declaration of the benchmark settings (workload id)


    _DEFAULT_NAME = 'Unnamed scenario'

    @abstractmethod
    def __init__(self, scenario_name: str):
        self._scenario_name = scenario_name.strip()

    @property
    def name(self) -> str:
        """
        Name of the current scenario, used for identification in the results output
        :return: Name as string that's defined in a constructor or default name
        """
        return self._scenario_name if self._scenario_name else self._DEFAULT_NAME

    @abstractmethod
    def get_benchmark_workload(self) -> str:
        """
        Override this method to return the workload ID you want to be used for benchmark run at the next step
        :return: Workload ID as it's defined in the Registry
        """
        raise NotImplementedError

    @abstractmethod
    def get_datagenerator_workload(self) -> str:
        """
        Override this method to return the workload ID you want to be used for data generation
        :return: Workload ID as it's defined in the Registry
        """
        raise NotImplementedError

    @abstractmethod
    def get_agents(self) -> List[ABCAgent]:
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

    def get_full_observation(self, registry_read: PRegistryReadClient) -> np.array:
        """
        Generate full observation space of the scenario.
        Override this method in case you require custom logic of forming full observation space.
        :param registry_read: Read-only client to the registry.
        :return: Array of the observation space
        """
        call_result = registry_read.read_get_all_fragments_with_meta(local_meta=None, global_meta=None)
        if call_result.failed:
            raise MddeError(call_result.error)

        return call_result  # TODO: Proper return conversion
