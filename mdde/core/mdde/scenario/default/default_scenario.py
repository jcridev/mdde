from typing import Tuple, Union, Sequence, Dict

from mdde.agent.abc import ABCAgent
from mdde.fragmentation.default import DefaultFragmenter, DefaultFragmentSorter
from mdde.fragmentation.protocol import PFragmentSorter, PFragmenter
from mdde.scenario.abc import ABCScenario


class DefaultScenario(ABCScenario):
    """
    Full observation space multi-agent data distribution scenario with read-only workload
    """

    def __init__(self, num_fragments: int, agents: Sequence[ABCAgent]):
        super().__init__('Default scenario')
        self._default_workload = 'read10000'

        self._num_fragments = num_fragments
        self._agents = tuple(agents)

    def get_benchmark_workload(self) -> str:
        return self._default_workload

    def get_data_generator_workload(self) -> str:
        return self._default_workload

    def get_fragmenter(self) -> PFragmenter:
        return DefaultFragmenter(self._num_fragments)

    def get_fragment_sorter(self) -> PFragmentSorter:
        return DefaultFragmentSorter()

    def get_agents(self) -> Tuple[ABCAgent]:
        return self._agents

    def get_fragment_instance_meta_fields(self) -> Union[Sequence, None]:
        return None

    def get_fragment_global_meta_fields(self) -> Union[Sequence, None]:
        return None

    def make_collective_step(self, actions: Dict[int, int]) -> None:
        pass

    def get_reward(self) -> Dict[int, float]:
        pass
