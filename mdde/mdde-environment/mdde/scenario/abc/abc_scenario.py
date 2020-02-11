from abc import ABC, abstractmethod
from typing import List

from mdde.agent import ABCAgent


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


    _DEFAULT_NAME = 'Unidentified scenario'

    @abstractmethod
    def __init__(self, scenario_name: str):
        self._scenario_name = scenario_name.strip()


    @property
    def name(self) -> str:
        """
        Name of the current scenario, used for identification in the results output
        :return:
        """
        return self._scenario_name if self._scenario_name is not None else self._DEFAULT_NAME

    @abstractmethod
    def get_bench_workload(self) -> str:
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