from typing import Sequence, Set
import numpy as np
from mdde.agent.abc import ABCAgent


class DefaultAgent(ABCAgent):
    def __init__(self, agent_id: str, data_node_ids: Set[str]):
        super().__init__(agent_id, data_node_ids)

    def get_actions(self) -> Sequence[int]:
        pass

    def filter_observation(self, full_observation: np.array) -> np.array:
        """
        Default agents can observe the full observation space
        :param full_observation: Full observation space provided by the environment
        :return: full_observation
        """
        return full_observation
