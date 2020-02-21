from typing import TypeVar, Generic

T = TypeVar('T')


class RegistryResponse(Generic[T]):
    """
    MDDE Registry API response wrapper
    """

    def __init__(self, result: T, error: str, error_code: int):
        """
        Constructor
        :param result: Result value of the registry call
        :param error: If there was an error, otherwise None
        :param error_code: Error code assigned to the specific type of the returned error. Take a look at
        mdde.registry.exceptions.EErrorCode in the registry Java source code for a full list of possible error codes
        """
        self._result: T = result
        self._error: str = error
        self._error_code: int = error_code

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
    def error_code(self) -> int:
        """
        If there was an error during the execution of the call, the error message is returned. Otherwise None
        :return:
        """
        return self._error_code

    @property
    def failed(self) -> bool:
        return self._error is not None


