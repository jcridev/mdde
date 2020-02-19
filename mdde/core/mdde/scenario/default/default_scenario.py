from typing import List

import numpy as np

from mdde.agent.abc import ABCAgent
from mdde.fragmentation.default import DefaultFragmenter, DefaultFragmentSorter
from mdde.fragmentation.protocol import PFragmentSorter, PFragmenter
from mdde.scenario.abc import ABCScenario


class DefaultScenario(ABCScenario):
    NUM_FRAGMENTS = 10

    def __init__(self):
        super().__init__('Default scenario')
        self._default_workload = 'read10000'

    def get_benchmark_workload(self) -> str:
        return self._default_workload

    def get_datagenerator_workload(self) -> str:
        return self._default_workload

    def get_fragmenter(self) -> PFragmenter:
        return DefaultFragmenter(self.NUM_FRAGMENTS)

    def get_fragment_sorter(self) -> PFragmentSorter:
        return DefaultFragmentSorter()

    def get_agents(self) -> List[ABCAgent]:
         # TODO
        pass
