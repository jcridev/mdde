from abc import ABC, abstractmethod


class ABCScenario(ABC):
    # TODO: Declaration of the agents
    #   TODO: mapping agents to data nodes
    #   TODO: actions definitions
    # TODO: Declaration of the meta values (global and local)
    # TODO: Declaration of the fragments generator
    # TODO: Declaration of the benchmark settings (workload id)


    _DEFAULT_NAME = 'Unidentified scenario'

    @abstractmethod
    def __init__(self, scenario_name: str):
        self._scenario_name = scenario_name

    @property
    def name(self) -> str:
        """
        Name of the current scenario, used for identification in the results output
        :return:
        """
        return self._scenario_name if self._scenario_name is not None else self._DEFAULT_NAME

    @abstractmethod
    def get_workload(self) -> str:
        """
        Retrieve the workload id that should be used during the next benchmark run
        :return:
        """
        raise NotImplementedError
