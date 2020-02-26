from typing import NamedTuple


class NodeAgentMapping(NamedTuple):
    """
    A mapping container from Agent ID to Data node ID
    """
    agent_id: int
    node_id: str
