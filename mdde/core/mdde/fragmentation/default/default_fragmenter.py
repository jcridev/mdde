import itertools
import logging
import math
from typing import Dict, Set, Sequence, Tuple
from functools import reduce

from mdde.fragmentation.exceptions import FragmentationError
from mdde.fragmentation.protocol import PFragmenter
from mdde.registry.container import RegistryResponseHelper
from mdde.registry.protocol import PRegistryReadClient, PRegistryWriteClient


class DefaultFragmenter(PFragmenter):
    """
    Default fragment generator. Simplest possible implementation.
    Attempts to generate a set number of fragments of equal or relatively equal size.
    Generates fragments with sequential numeric IDs encoded as strings. Assumed that the collection of tuples is able
    to fit in RAM.

    Intended scenario for this fragmenter is where all nodes contain each the same amount of tuples with no duplicates.
    In other cases might result in unbalanced fragmentation.

    The fragmenter will not attempt to balance out fragments through shuffling of tuples.

    Fragment keys are numeric sequence and should be used for ordering of retrieved fragments.
    """

    def __init__(self, number_of_fragments: int):
        """
        Constructor
        :param number_of_fragments: Desired number of fragments to be formed. Must be > 0 and >= number of tuples
        """
        if number_of_fragments < 1:
            raise ValueError("Target number of fragments must be larger than 0")
        self._target_n_fragments = number_of_fragments
        self._logger = logging.getLogger('DefaultFragmenter')

    def run_fragmentation(self, registry_reader: PRegistryReadClient, registry_writer: PRegistryWriteClient) \
            -> Tuple[bool, Tuple[str, ...]]:
        """
        Execute default fragmentation
        :param registry_reader: Read access to the registry
        :param registry_writer: Write access to the registry
        :return: Always returns false as the default fragmenter doesn't perform any tuple shuffling.
        """
        # Get registry nodes
        nodes_response = registry_reader.read_nodes()
        RegistryResponseHelper.raise_on_error(nodes_response)
        self._logger.debug("Fragmenter retrieved nodes: %d", len(nodes_response.result))
        # Retrieve all unassigned fragments
        node_contents = {}  # node_id:Set[tuple_id]
        for node in nodes_response.result:
            # Get all tuple IDs that are located on the node but not assigned to a fragment
            utuples_response = registry_reader.read_node_unassigned_tuples(node)
            RegistryResponseHelper.raise_on_error(utuples_response)
            node_contents[node] = set(utuples_response.result)

        total_num_tuples = reduce(lambda a, b: (a if isinstance(a, int) else len(a)) + len(b), node_contents.values())
        if total_num_tuples < self._target_n_fragments:
            err = FragmentationError("Specified target number of fragments {} is higher than the number of tuples in "
                                     "the registry: {}.".format(self._target_n_fragments, total_num_tuples))
            self._logger.error(err)
            raise err

        optimal_fragment_size = round(total_num_tuples / self._target_n_fragments)

        # If there are duplicates of fragments on different nodes we assign them to the same fragment
        node_keys = node_contents.keys()
        node_keys_processed = []
        sequential_fragment_id = itertools.count()
        created_fragments = 0
        fragment_ids_ordered = list()
        for processing_node in node_keys:
            processing_node_tuples = node_contents[processing_node]
            for other_node_key, other_node_tuples in [(k, v) for k, v in node_contents.items()
                                                      if k != processing_node and k not in node_keys_processed]:
                intersection = processing_node_tuples.intersection(other_node_tuples)
                if len(intersection) < 1:
                    continue
                # Find nodes that contain the same intersection and verify possibility of making fragments of these
                a_nodes = self._find_nodes_containing({k: v for k, v in node_contents.items()
                                                       if k != processing_node and k != other_node_key}, intersection)
                # Form fragments with the discovered intersection
                if len(intersection) <= optimal_fragment_size:
                    # Intersection is smaller than expected or exactly the expected size
                    created_fragments = next(sequential_fragment_id)
                    self._form_fragment(registry_writer,
                                        str(created_fragments),
                                        set(intersection),
                                        optimal_fragment_size)

                elif len(intersection) > optimal_fragment_size:
                    # Intersection is larger than expected, try to make fragments of the relatively same length
                    number_of_splits = math.ceil(len(intersection) / optimal_fragment_size)
                    splits = self._split_list(intersection, number_of_splits)
                    for split in splits:
                        created_fragments = next(sequential_fragment_id)
                        self._form_fragment(registry_writer,
                                            str(created_fragments),
                                            set(split),
                                            optimal_fragment_size)
                node_keys_processed.append(processing_node)
                self._clear_out_processed(node_contents, intersection)

        # Optimal split
        non_empty_node_contents = {k: v for k, v in node_contents.items()
                                   if len(v) > 0}
        remaining_fragments = self._target_n_fragments - created_fragments
        node_splits = {}
        cnt_uprocessed_non_empty_nodes = len(non_empty_node_contents)
        for node_key, node_tuples \
                in {k: v for k, v in sorted(non_empty_node_contents.items(),
                                            key=lambda item: len(item[1]),
                                            reverse=False)}.items():
            number_of_splits = int(round(len(node_tuples) / optimal_fragment_size))
            if number_of_splits == 0:
                number_of_splits = 1

            temp_remaining_fragments = remaining_fragments - number_of_splits
            if number_of_splits > 1 and temp_remaining_fragments < cnt_uprocessed_non_empty_nodes - 1:
                number_of_splits = number_of_splits - cnt_uprocessed_non_empty_nodes - 1

            if (remaining_fragments - number_of_splits * cnt_uprocessed_non_empty_nodes) > 0:
                number_of_splits += 1

            remaining_fragments = remaining_fragments - number_of_splits
            node_splits[node_key] = number_of_splits
            cnt_uprocessed_non_empty_nodes -= 1

        # Process remaining unassigned tuples
        for node_key, node_tuples in node_contents.items():
            splits = self._split_list(list(node_tuples), node_splits[node_key])
            for split in splits:
                next_frag_id = str(next(sequential_fragment_id))
                self._form_fragment(registry_writer,
                                    next_frag_id,
                                    set(split),
                                    optimal_fragment_size)
                fragment_ids_ordered.append(next_frag_id)

        return False, tuple(fragment_ids_ordered)  # We don't modify the registry outside of formation of fragments

    @staticmethod
    def _find_nodes_containing(nodes: Dict[str, Set[str]], items: [str]) -> []:
        items_found_at = []
        for node_key, node_tuples in nodes.items():
            intersection = node_tuples.intersection(items)
            if len(intersection) > 0:
                if len(intersection) < len(items):
                    raise FragmentationError("Unable to form fragments, expected intersection length {}"
                                             ", while real is {}".format(len(items), len(intersection)))
                items_found_at.append(node_key)
        return items_found_at

    @staticmethod
    def _clear_out_processed(nodes: Dict[str, Set[str]], items: [str]):
        for node_key, node_values in nodes.items():
            node_values.discard(items)

    def _form_fragment(self, registry_writer: PRegistryWriteClient,
                       new_frag_id: str,
                       tuples: Set[str],
                       optimal_fragment_size: int):
        if len(tuples) < optimal_fragment_size:
            self._logger.warning("Forming a fragment based on intersecting tuples with a suboptimal length "
                                 "of {} instead of the optimal {}".format(len(tuples), optimal_fragment_size))
        registry_writer.write_fragment_create(new_frag_id, tuples)

    @staticmethod
    def _split_list(items: [], target_splits: int) -> Sequence[Sequence[str]]:
        result = []
        for i in reversed(range(1, target_splits + 1)):
            split_point = len(items) // i
            result.append(items[:split_point])
            items = items[split_point:]
        return result
