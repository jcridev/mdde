import itertools
import logging
import math
from typing import Dict, Set, Sequence
from functools import reduce

from mdde.fragmentation.exceptions import FragmentationError
from mdde.fragmentation.protocol import PFragmenter
from mdde.registry.container import RegistryResponseHelper
from mdde.registry.protocol import PRegistryReadClient, PRegistryWriteClient


class DefaultFragmenter(PFragmenter):
    """
    Default fragment generator. Attempts to generate fragments of equal size.
    Generates fragments with sequential numeric IDs encoded as strings. Assumed that the collection of tuples is able
    to fit in RAM.
    """

    def __init__(self, number_of_fragments: int):
        """
        Constructor
        :param number_of_fragments: Desired number of fragments to be formed. Must be > 0 and >= number of tuples
        """
        if number_of_fragments < 1:
            raise ValueError("Target number of fragments must be larger than 0")

        self._target_n_fragments = number_of_fragments

    def run_fragmentation(self, registry_reader: PRegistryReadClient, registry_writer: PRegistryWriteClient) -> bool:
        # Get registry nodes
        nodes_response = registry_reader.read_nodes()
        RegistryResponseHelper.throw_on_error(nodes_response)
        logging.debug("Fragmenter retrieved nodes: {}", len(nodes_response.result))
        # Retrieve all unassigned fragments
        node_contents = {}  # node_id:Set[tuple_id]
        for node in nodes_response.result:
            # Get all tuple IDs that are located on the node but not assigned to a fragment
            utuples_response = registry_reader.read_node_unassigned_tuples(node)
            RegistryResponseHelper.throw_on_error(utuples_response)
            node_contents[node] = utuples_response.result

        total_num_tuples = reduce(lambda a, b: len(a) + len(b), node_contents.values())
        if total_num_tuples < self._target_n_fragments:
            raise ValueError("Specified target number of fragments {} is higher than the number of tuples in the "
                             "registry: {}.".format(self._target_n_fragments, total_num_tuples))

        optimal_fragment_size = round(total_num_tuples / self._target_n_fragments)

        # If there are duplicates of fragments on different nodes we assign them to the same fragment
        node_keys = node_contents.keys()
        node_keys_processed = []
        sequential_fragment_id = itertools.count()
        for processing_node in node_keys:
            processing_node_tuples = node_contents[processing_node]
            for other_node_key, other_node_tuples in {k: v for k, v in node_contents.items()
                                                      if k != processing_node and k not in node_keys_processed}:
                intersection = processing_node_tuples.intersection(other_node_tuples)
                if len(intersection) < 1:
                    continue
                # Find nodes that contain the same intersection and verify possibility of making fragments of these
                a_nodes = self._find_nodes_containing({k: v for k, v in node_contents.items()
                                                       if k != processing_node and k != other_node_key}, intersection)
                # Form fragments with the discovered intersection
                if len(intersection) <= optimal_fragment_size:
                    # Intersection is smaller than expected or exactly the expected size
                    self._form_fragment(registry_writer,
                                        str(next(sequential_fragment_id)),
                                        set(intersection),
                                        optimal_fragment_size)

                elif len(intersection) > optimal_fragment_size:
                    # Intersection is larger than expected, try to make fragments of the relatively same length
                    number_of_splits = math.ceil(optimal_fragment_size / len(intersection))
                    splits = self._split_list(intersection, number_of_splits)
                    for split in splits:
                        self._form_fragment(registry_writer,
                                            str(next(sequential_fragment_id)),
                                            set(split),
                                            optimal_fragment_size)
                node_keys_processed.append(processing_node)
                self._clear_out_processed(node_contents, intersection)

        # Process remaining unassigned tuples
        for node_key, node_tuples in node_contents:
            number_of_splits = math.ceil(optimal_fragment_size / len(node_tuples))
            splits = self._split_list(node_tuples, number_of_splits)
            for split in splits:
                self._form_fragment(registry_writer,
                                    str(next(sequential_fragment_id)),
                                    set(split),
                                    optimal_fragment_size)

        return False  # We don't modify the registry outside of formation of fragments

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

    @staticmethod
    def _form_fragment(registry_writer: PRegistryWriteClient,
                       new_frag_id: str,
                       tuples: Set[str],
                       optimal_fragment_size: int):
        logging.warning("Forming a fragment based on intersecting tuples with a suboptimal length "
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
