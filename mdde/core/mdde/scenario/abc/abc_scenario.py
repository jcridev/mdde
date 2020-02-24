from abc import ABC, abstractmethod
from typing import Tuple, Union, Sequence

import numpy as np

from mdde.agent.abc import ABCAgent
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
    def get_data_generator_workload(self) -> str:
        """
        Override this method to return the workload ID you want to be used for data generation
        :return: Workload ID as it's defined in the Registry
        """
        raise NotImplementedError

    @abstractmethod
    def get_agents(self) -> Tuple[ABCAgent]:
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

    @abstractmethod
    def get_fragment_instance_meta_fields(self) -> Union[Sequence, None]:
        """
        Override to return a list of meta values that are attached to every instance of a fragment.
        Return None if no meta values needed
        :return:
        """
        raise NotImplementedError

    @abstractmethod
    def get_fragment_global_meta_fields(self) -> Union[Sequence, None]:
        """
        Override to return a list of meta values that are attached to a fragment globally.
        Return None if no meta values needed
        :return:
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
            raise RuntimeError(call_result.error)
        fragment_catalog = call_result.result
        # make sure fragments are ordered
        sorted_fragments = self.get_fragment_sorter().sort(fragment_catalog['fragments'])

        #obs_full = {}
        #for agent in self.get_agents():


        return call_result  # TODO: Proper return conversion
