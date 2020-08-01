from typing import TypeVar, Generic, Union

from mdde.core.exception import RegistryErrorCodes

T = TypeVar('T')


class RegistryResponse(Generic[T]):
    """
    MDDE Registry API response wrapper.
    """

    __slots__ = ['_result', '_error', '_error_code']

    def __init__(self, result: T, error: str, error_code: Union[int, RegistryErrorCodes]):
        """
        Constructor.
        :param result: Result value of the registry call.
        :param error: If there was an error, otherwise None.
        :param error_code: Error code assigned to the specific type of the returned error. Take a look at
        mdde.registry.exceptions.RegistryErrorCodes in the registry Java source code for a full list of possible error
        codes.
        """
        self._result: T = result
        self._error: str = error
        if isinstance(error_code, RegistryErrorCodes):
            self._error_code = error_code
        elif error_code is not None:
            self._error_code: RegistryErrorCodes = RegistryErrorCodes(error_code)

    @property
    def result(self) -> T:
        """
        Result of the registry call execution.
        :return: Registry response container.
        """
        return self._result

    @property
    def error(self) -> Union[None, str]:
        """
        Error text.
        :return: If there was an error during the execution of the call, the error message is returned. Otherwise None.
        """
        return self._error

    @property
    def error_code(self) -> Union[None, RegistryErrorCodes]:
        """
        Error code.
        :return: If there was an error during the execution of the call, the error message is returned. Otherwise None.
        """
        return self._error_code

    @property
    def failed(self) -> bool:
        """
        Get if the response contains an error.
        :return: True - Result contains an error.
        """
        return self._error is not None and self._error_code is not None

    @property
    def is_constraint_error(self) -> bool:
        """
        Determine if the error code, if set, is a logic constraint and not a runtime error.
        :return: True - current error code is within the range of errors that constitute a logical registry constraint.
        This means that the operation was denied but the reason is not a code related error but the illegality of the
        operation within the current environment state (ex. removal of the unique fragment exemplar, local replication).
        """
        if not self.failed:
            return False
        if self._error_code.value in range(2600, 2999):
            return True
        return False
