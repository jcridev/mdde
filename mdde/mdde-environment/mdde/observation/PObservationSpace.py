from abc import abstractmethod
from typing import Protocol, List

from mdde.common.data import Fragment
from mdde.observation.filter import PObservationFilter


class PObservationSpace(Protocol):
    """Observation space protocol, keep track of all observations within the environment"""

    @abstractmethod
    def get_full(self) -> {int, List[Fragment]}:
        """
        Get full list of agents with all of the fragments stored within these agents
        :return: {actor_id, [fragments_stored_within_the_actor]}
        """
        raise NotImplementedError()

    @abstractmethod
    def get_for_agent(self, actor_id: int, observation_filter: PObservationFilter = None) -> List[Fragment]:
        raise NotImplementedError()
