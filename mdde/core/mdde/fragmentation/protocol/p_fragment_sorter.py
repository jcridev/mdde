from abc import abstractmethod
from typing import List, Sequence

from mdde.core.fragment import Fragment

try:
    from typing import Protocol
except ImportError:
    from typing_extensions import Protocol


class PFragmentSorter(Protocol):

    @abstractmethod
    def sort(self, fragments: Sequence[Fragment]) -> List[Fragment]:
        """
        Override this method to provide the logic in accordance to which fragments must be sorted. Depending on the
        implementation of your RL logic, this ordering might need to be stable across all of the learning steps.
        :param fragments: Sequence of fragments, assumed unordered
        :return: Ordered list of fragments
        """
        raise NotImplementedError
