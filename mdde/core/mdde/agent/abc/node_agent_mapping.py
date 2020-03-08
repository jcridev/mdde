from typing import NamedTuple


class NodeAgentMapping(NamedTuple):
    """
    A mapping container from Agent ID to Data node ID
    """
    agent_id: int
    node_id: str

    def __eq__(self, other):
        return self.agent_id == other.agent_id \
               and self.node_id == other.node_id
