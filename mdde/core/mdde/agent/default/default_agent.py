from typing import List, Tuple, Sequence, NamedTuple, Union

import numpy as np

from mdde.agent.abc import ABCAgent, NodeAgentMapping
from mdde.agent.enums import EActionResult


class DefaultAgent(ABCAgent):

    def __init__(self,
                 agent_name: str,
                 agent_id: int,
                 data_node_ids: List[str],
                 group: str = ABCAgent.DEFAULT_GROUP):
        super().__init__(agent_name, agent_id, data_node_ids, group)
        self._actions: Union[np.ndarray, None] = None
        """Agent's action space"""

    class Action(NamedTuple):
        """
        Specific action for the action space.

        node_source_id: Node where the action is originated (where the tuple is removed from if del operation)
        node_destination_id: Destination node for copy operations
        fragment_id: Id of the affected fragment
        is_del: del for own nodes, false - meaning it's a copy action (foreign nodes)
        """
        node_source_id: Union[str, None]
        node_destination_id: Union[str, None]
        fragment_id: Union[str, None]
        is_del: bool

    def get_actions(self) -> int:
        """Number of actions where indexes of actions are within [0, number_of_actions)"""
        return len(self._actions)

    def create_action_space(self,
                            nodes: Tuple[NodeAgentMapping, ...],
                            fragments: Sequence[str],
                            obs: np.ndarray,
                            ) -> int:
        """
        Default agent can remove fragment from self and can also copy fragment from other to self.
        Action space: 1 + f*n_f*n_o + f*n_o*n_o + f*n_o,
        where f is the number of fragments, n_f number of foreign nodes, n_o number of own nodes
        0 - do nothing
        f*n_o - remove from self
        f*n_o*n_o - copy within self
        f*n_f*n_o - copy from others
        We assume that no fragments are created or fully removed without a trace for the default agent.
        """
        a_actions = np.empty(1 + len(nodes) * len(fragments), dtype=object)
        a_actions[0] = self.Action(node_source_id=None,
                                   node_destination_id=None,
                                   fragment_id=None, is_del=False)  # do nothing action
        act_cnt: int = 1
        for node in nodes:
            is_own_node = node.node_id in self.data_node_ids
            if not is_own_node:
                # we can only copy from a foreign node to own node
                for own_node in self.data_node_ids:
                    for fragment in fragments:
                        # copy action from foreign to own node
                        a_actions[act_cnt] = self.Action(node_source_id=node.node_id,
                                                         node_destination_id=own_node,
                                                         fragment_id=fragment,
                                                         is_del=False)
                        act_cnt += 1
            else:
                # we can copy fragments between own nodes and remove fragments from own nodes
                for own_node in [n for n in self.data_node_ids if n != node.node_id]:
                    for fragment in fragments:
                        # copy action from own to own node
                        a_actions[act_cnt] = self.Action(node_source_id=node.node_id,
                                                         node_destination_id=own_node,
                                                         fragment_id=fragment,
                                                         is_del=False)
                        act_cnt += 1
                for fragment in fragments:
                    # delete action from own node
                    a_actions[act_cnt] = self.Action(node_source_id=node.node_id,
                                                     node_destination_id=None,
                                                     fragment_id=fragment,
                                                     is_del=True)
                    act_cnt += 1

        self._actions = a_actions
        return len(self._actions)

    def do_action(self, action_id: int) -> EActionResult:
        """
        Execute the selected action
        :param action_id: Index of the action within [0, number_of_actions)
        :return: EActionResult value
        :raises: In case action resulted in a runtime or code level error that is not an expected logical constraint
        violation error.
        """
        if self.get_actions() <= action_id or action_id < 0:
            raise IndexError("Action id '{}' is out of actions space: 0 - {}".format(action_id, self._actions))

        if action_id == 0:
            return EActionResult.done  # do nothing (agent is done for the learning round)

        selected_action: DefaultAgent.Action = self._actions[action_id]

        if selected_action.is_del:
            # delete action
            action_result = self._invoke_delete_from_self(selected_action.node_source_id,
                                                          selected_action.fragment_id)
        else:
            # copy actions
            action_result = self._invoke_copy_to_self(selected_action.node_source_id,
                                                      selected_action.node_destination_id,
                                                      selected_action.fragment_id)
        if action_result:
            return EActionResult.ok
        return EActionResult.denied

    def filter_observation(self, obs_descr: Tuple[NodeAgentMapping, ...], obs: np.ndarray) -> np.ndarray:
        """Return full observation space without modifications"""
        return obs

    def _invoke_copy_to_self(self, source_node: str, destination_node: str, fragment: str) -> bool:
        copy_result = self._registry_write.write_fragment_replicate(fragment, source_node, destination_node)
        if not copy_result.failed:
            return True
        if copy_result.is_constraint_error:
            return False
        raise RuntimeError(copy_result.error)

    def _invoke_delete_from_self(self, node: str, fragment: str) -> bool:
        delete_result = self._registry_write.write_fragment_delete_exemplar(fragment, node)
        if not delete_result.failed:
            return True
        if delete_result.is_constraint_error:
            return False
        raise RuntimeError(delete_result.error)
