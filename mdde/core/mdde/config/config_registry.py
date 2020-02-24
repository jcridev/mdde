from typing import Tuple, NamedTuple

import yaml


class ConfigRegistry:
    """
    Registry configuration file parser.
    Useful in cases when you want to pull nodes from it and auto assign them to agents instead of specifying them
    manually in the scenario definition.
    """

    class DataNode(NamedTuple):
        """
        Data node information as defined in the registry config
        """
        id: str  # node id
        default: bool  # default nodes, are the ones that are created when the environment is initially created

    def __init__(self):
        self._data_node_ids: Tuple[ConfigRegistry.DataNode] = tuple()

    def read(self, file_path: str):
        """
        Fill the object form a yml file
        :param file_path:
        :return:
        """
        with open(file_path, 'r') as stream:
            yml_file = yaml.safe_load(stream)
            # only 2 values are relevant for the environment: id and whether or not the node is a default one
            self._data_node_ids = (self.DataNode(node['id'], node['default']) for node in yml_file['nodes'])

    def get_nodes(self) -> Tuple[DataNode]:
        return self._data_node_ids
