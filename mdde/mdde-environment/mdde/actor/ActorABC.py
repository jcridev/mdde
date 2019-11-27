from abc import ABC, abstractmethod

from mdde.common.data.Fragment import Fragment, FragmentData
from mdde.core.exception.ActorInitializationError import ActorInitializationError


class ActorABC(ABC):
    """Base class for all agent actors"""

    _id: int
    _name: str

    def __init__(self, a_id: int, a_name: str = None):
        """
        Initializer
        :param a_id: Numerical agent id, must be unique within the environment
        :param a_name: Optional agent name for simplification of identification in logs and debug (kept it short)
        """
        # Prevent instantiation of the abstract class
        if type(self) is ActorABC:
            raise ActorInitializationError("ActorABC is an abstract class, "
                                           "subclass it with overriding all methods marked as @abstractmethod")
        # Check id for correctness
        if a_id is None:
            raise ValueError("Actor id can't be None")
        if not isinstance(a_id, int):
            raise TypeError("Actor id must be Integer")
        self._id = a_id

        # If name wasn't passed use id as a name
        if a_name is None:
            self._name = str(a_id)
        else:
            if not isinstance(a_name, str):
                self._name = str(a_name)
            self._name = a_name

    @property
    def id(self) -> int:
        """Id of the agent actor, unique within any given environment, used for identification of the actor"""
        return self._id

    @property
    def name(self) -> str:
        """Name of the agent, used to simplify debugging and logs readability"""
        return self._name

    @abstractmethod
    def configure(self, **kwargs):
        """
        Any custom agent configuration should be done here.
        For example: database connection settings set up

        :param kwargs: Key-value arguments list
        """
        raise NotImplementedError()

    @abstractmethod
    def reset(self):
        """
        Clear the underlying data store removing all records, statistics, etc.
        This method should return the actor into its initial state.
        """
        raise NotImplementedError()

    @abstractmethod
    def read_fragment(self, f_key: int) -> Fragment:
        """
        Retrieve Fragment from the agent (without data).
        Used for point retrieval of meta data ().

        :param f_key: Fragment hash key value
        :return: Fragment
        """
        raise NotImplementedError()

    @abstractmethod
    def read_fragment_data(self, f_key: int) -> FragmentData:
        """
        Retrieve Fragment from the agent (with data).
        Used for making copies or moving data between agents.

        :param f_key: Fragment hash key value
        :return: FragmentData
        """
        raise NotImplementedError()

    def _attach_environment(self, c_env):
        raise NotImplementedError()
