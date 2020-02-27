from typing import List, Tuple, Sequence, NamedTuple, Union

import numpy as np

#from mdde.registry.container import RegistryResponseHelper
from mdde.agent.abc import ABCAgent, NodeAgentMapping


class DefaultAgent(ABCAgent):

    def __init__(self, agent_name: str, agent_id: int, data_node_ids: List[str]):
        super().__init__(agent_name, agent_id, data_node_ids)
        self._actions: Union[np.ndarray, None] = None

    class Action(NamedTuple):
        node_source_id: Union[str, None]
        node_destination_id: Union[str, None]
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
            is_own_node = node.node_id in self.get_data_node_ids
            if not is_own_node:
                # we can only copy from a foreign node to own node
                for own_node in self.get_data_node_ids:
                    for fragment in fragments:
                        # copy action from foreign to own node
                        a_actions[act_cnt] = self.Action(node_source_id=node.node_id,
                                                         node_destination_id=own_node,
                                                         fragment_id=fragment,
                                                         is_del=False)
                        act_cnt += 1
            else:
                # we can copy fragments between own nodes and remove fragments from own nodes
                for own_node in [n for n in self.get_data_node_ids if n != node.node_id]:
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

    def do_action(self, action_id: int):
        if self._actions <= action_id or action_id < 0:
            raise IndexError("Action id '{}' is out of actions space: 0 - {}".format(action_id, self._actions))

        if action_id == 0:
            return  # do nothing

        selected_action: DefaultAgent.Action = self._actions[action_id]

        if selected_action.is_del:
            # delete action
            self._invoke_delete_from_self(selected_action.node_source_id,
                                          selected_action.fragment_id)
        else:
            # copy actions
            self._invoke_copy_to_self(selected_action.node_source_id,
                                      selected_action.node_destination_id,
                                      selected_action.fragment_id)

    def filter_observation(self, obs_descr: Tuple[NodeAgentMapping, ...], obs: np.ndarray) -> np.ndarray:
        """
        Return full observation space
        """
        return obs

    def _invoke_copy_to_self(self, source_node: str, destination_node: str, fragment: str):
        copy_result = self._registry_write.write_fragment_replicate(fragment, source_node, destination_node)
        #RegistryResponseHelper.raise_on_error(copy_result)

    def _invoke_delete_from_self(self, node: str, fragment: str):
        delete_result = self._registry_write.write_fragment_delete_exemplar(fragment, node)
        #RegistryResponseHelper.raise_on_error(delete_result)
