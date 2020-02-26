from abc import abstractmethod
from typing import Tuple, Sequence

try:
    from typing import Protocol
except ImportError:
    from typing_extensions import Protocol


class PFragmentSorter(Protocol):

    @abstractmethod
    def sort(self, fragments: Sequence[str]) -> Tuple[str, ...]:
        """
        Override this method to provide the logic in accordance to which fragments must be sorted. Depending on the
        implementation of your RL logic, this ordering might need to be stable across all of the learning steps.
        :param fragments: Sequence of fragments, assumed unordered
        :return: Ordered list of fragments
        """
        raise NotImplementedError
