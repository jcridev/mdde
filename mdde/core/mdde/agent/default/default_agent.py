from typing import Sequence, List, Tuple
import numpy as np

from mdde.agent.abc import ABCAgent, NodeAgentMapping


class DefaultAgent(ABCAgent):
    def __init__(self, agent_id: str, data_node_ids: List[str]):
        super().__init__(agent_id, data_node_ids)

    def get_actions(self) -> Sequence[int]:
        pass

    def filter_observation(self, obs_descr: Tuple[NodeAgentMapping, ...], obs: np.array) -> np.array:
        """
        Return full observation space
        :param obs_descr:
        :param obs:
        :return:
        """
        return obs
