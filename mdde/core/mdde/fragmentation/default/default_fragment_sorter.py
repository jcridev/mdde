from typing import Sequence, Tuple
import natsort

from mdde.fragmentation.protocol import PFragmentSorter


class DefaultFragmentSorter(PFragmentSorter):
    """
    Default sorter of fragments. Sorts fragment by IDs in ascending numeric or alphabetic order.
    Suitable for the scenarios where the number of fragments is static (fragmentation is executed once and the generated
    fragments do no change over the course of the scenario run).
    """
    def sort(self, fragments: Sequence[str]) -> Tuple[str, ...]:
        """
        Sort fragments to make sure that the ordering is stable. Note this function is meant for the static number
        of fragments and not suitable when the number is changing (fragments are created and removed.)
        :param fragments: Sequence of unordered fragment IDs.
        :return: Tuple of ordered fragment IDs.
        """
        return tuple(natsort.natsorted(fragments, key=lambda fragment: fragment))
