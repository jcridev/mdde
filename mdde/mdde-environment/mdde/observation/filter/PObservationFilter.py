from abc import abstractmethod
from typing import Protocol, List

from mdde.common.data import Fragment

class PObservationFilter(Protocol):  # TODO: Observation filtering protocol per actor basis
    @abstractmethod
    def get_observations(self, full_observation) -> List[Fragment]:
        """Convert full observation space into a filtered one"""
        raise NotImplementedError()
