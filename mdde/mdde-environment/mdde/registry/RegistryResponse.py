from typing import TypeVar, Generic

T = TypeVar('T')


class RegistryResponse(Generic[T]):
    """
    MDDE Registry API response wrapper
    """

    def __init__(self, result: T, error: str) -> None:
        """
        Constructor
        :param result: Result value of the registry call
        :param error: If there was an error, otherwise None
        """
        self._result: T = result
        self._error: str = error

    @property
    def result(self) -> T:
        """
        Result of the registry call execution
        :return:
        """
        return self._result

    @property
    def error(self) -> str:
        """
        If there was an error during the execution of the call, the error message is returned. Otherwise None
        :return:
        """
        return self._error

    @property
    def failed(self) -> bool:
        return self._error is not None


