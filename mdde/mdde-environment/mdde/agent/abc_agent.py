from abc import ABC, abstractmethod
from typing import List


class ABCAgent(ABC):
    @abstractmethod
    def __init__(self, agent_id: str):
        """
        Constructor
        :param agent_id: Unique agent id (name)
        """
        self._agent_id = agent_id

    def id(self) -> str:
        """
        Get agent id
        :return: String agent id
        """
        return self._agent_id

    def get_actions(self) -> List[int]:
        """
        Get the list of action ids available to the agent
        :return:
        """
        raise NotImplementedError

