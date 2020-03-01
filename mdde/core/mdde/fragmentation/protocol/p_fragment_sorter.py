from abc import abstractmethod
from typing import Tuple, Sequence

try:
    from typing import Protocol
except ImportError:
    from typing_extensions import Protocol


class PFragmentSorter(Protocol):
    """
    It's important for the order of fragments to be stable. Fragment id's can be arbitrary and not directly mapped to
    any particular array, while at the same time learners require consistent observation and action spaces. Therefore,
    this protocol must be implemented in a way that makes sure all of the fragments retrieved from the registry are
    ordered correctly.

    The exact implementation depends solely on the scenario. For example, in the scenario where fragments are created
    once and then never removed and no new fragments added over the course of the learning run, it's enough to assign
    numeric values as fragment ids and sort them by these values. Because duplicate fragment ids aren't allowed, the
    result will be consistent every tim this sorter is used.

    However, if the scenario assumes the need to delete fragments out of the registry, an additional cache would be
    required to store the information about the removed fragments.
    """

    @abstractmethod
    def sort(self, fragments: Sequence[str]) -> Tuple[str, ...]:
        """
        Override this method to provide the logic in accordance to which fragments must be sorted. Depending on the
        implementation of your RL logic, this ordering must be stable across all of the learning steps.
        :param fragments: Sequence of fragments, assumed unordered
        :return: Ordered list of fragments
        """
        raise NotImplementedError
