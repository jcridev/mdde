from typing import List, Tuple, Sequence, NamedTuple, Union

import numpy as np

from mdde.agent.abc import ABCAgent, NodeAgentMapping


class DefaultAgent(ABCAgent):

    def __init__(self, agent_id: str, data_node_ids: List[str]):
        super().__init__(agent_id, data_node_ids)
        self._actions: Union[np.ndarray, None] = None

    class Action(NamedTuple):
        node_id: Union[str, None]
        fragment_id: Union[str, None]
        is_del: bool  # del for own nodes, false - meaning it's a copy action (foreign nodes)

    def get_actions(self) -> int:
        return len(self._actions)

    def create_action_space(self,
                            nodes: Tuple[NodeAgentMapping, ...],
                            fragments: Sequence[str],
                            obs: np.ndarray,
                            ) -> int:
        """
        Default agent can remove fragment from self and can also copy fragment from other to self.
        Action space: 1 + (n*f) + (a-1)*n*f, where f is the number of fragments, n number of nodes
        0 - do nothing
        (n*f) - remove from self
        (a-1)*n*f - copy from others
        We assume that no fragments are created or fully removed without a trace for the default agent.
        """
        a_actions = np.empty(1 + len(nodes) * len(fragments), dtype=object)
        a_actions[0] = self.Action(node_id=None, fragment_id=None, is_del=False)  # do nothing action
        act_cnt: int = 1
        for node in nodes:
            own_node = node.node_id in self.get_data_node_ids
            for fragment in fragments:
                a_actions[act_cnt] = self.Action(node_id=node.node_id, fragment_id=fragment, is_del=own_node)
                act_cnt += 1
        self._actions = a_actions
        return len(self._actions)

    def do_action(self, action_id: int):
        if self._actions <= action_id:
            raise IndexError("Action id '{}' is out of actions space: 0 - {}".format(action_id, self._actions))

        if action_id == 0:
            return  # do nothing

        mid_point = (self._actions+1) // 2
        if action_id < mid_point:
            # copy actions
            return NotImplementedError
        else:
            # delete action
            return NotImplementedError

    def filter_observation(self, obs_descr: Tuple[NodeAgentMapping, ...], obs: np.array) -> np.array:
        """
        Return full observation space
        """
        return obs
