from typing import Tuple, Sequence

import numpy as np

from . import DefaultAgent
from mdde.agent.abc import NodeAgentMapping


class SingleNodeDefaultAgent(DefaultAgent):
    """Default agent allowing only a single data node to be managed. Generates a uniform action space."""

    def __init__(self,
                 agent_name: str,
                 agent_id: int,
                 data_node_id: str,
                 group: str = DefaultAgent.DEFAULT_GROUP,
                 write_stats: bool = False):
        """
        Single node default agent constructor
        :param agent_name: Name of the agent instance.
        :param agent_id: Unique ID of the agent within the experimental run.
        :param data_node_id: Data node managed by the agent.
        :param group: (optional) Group of the agent.
        :param write_stats: (optional) If True - agent will write some data (description of it's action space), to the
        results folder provided in `self._config` for later analysis.
        """
        super().__init__(agent_name, agent_id, [data_node_id], group, write_stats)

    def create_action_space(self,
                            nodes: Tuple[NodeAgentMapping, ...],
                            fragments: Sequence[str],
                            obs: np.ndarray,
                            ) -> int:
        """
        Generates an action space size: 1 + len(nodes) * len(fragments) * 2.
        Action 0 is a do nothing action. For each node a two sets of actions will be created: copy a fragment to self,
        delete fragment from self. It's not allowed, however, to remove fragments from other agents or copy from node
        to the very same node. These constraints will be observed by the registry. It's beneficial to have ownership
        of the node indicated in the observation space.
        """
        own_node = self.data_node_ids[0]
        n_frags = len(fragments)
        a_actions = np.empty(1 + len(nodes) * n_frags * 2, dtype=object)
        act_idx_ref = 0
        for node in nodes:
            act_starting_point = n_frags * act_idx_ref * 2
            act_idx_ref += 1
            for frag_idx, frag_reg_id in enumerate(fragments, 1):
                ref_idx = frag_idx + act_starting_point
                # Copy
                a_actions[ref_idx] = self.Action(node_source_id=node.node_id,
                                                 node_destination_id=own_node,
                                                 fragment_id=frag_reg_id,
                                                 is_del=False)
                # Delete
                a_actions[ref_idx + n_frags] = self.Action(node_source_id=node.node_id,
                                                           node_destination_id=None,
                                                           fragment_id=frag_reg_id,
                                                           is_del=True)

        # do nothing action
        a_actions[0] = self.Action(node_source_id=None,
                                   node_destination_id=None,
                                   fragment_id=None, is_del=False)

        self._actions = a_actions
        if self._write_stats:  # Save descriptions for later analysis
            self._dumpActions()
        return len(self._actions)
