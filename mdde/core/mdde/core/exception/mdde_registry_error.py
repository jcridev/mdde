from typing import Union, AnyStr

from mdde.core.exception import MddeError, RegistryErrorCodes


class MddeRegistryError(MddeError):

    def __init__(self, error_code: Union[int, AnyStr], message=None):
        """
        Constructor
        :param error_code: As defined in dev/jcri/mdde/registry/exceptions/EErrorCode.java
        :param message: Optional string message describing the error
        """
        super(MddeRegistryError, self).__init__(message)

        if error_code is None:
            raise TypeError("Registry specific errors must always be supplied with the specific error code.")

        if isinstance(error_code, int):
            self._error_code = error_code
        else:
            int(error_code, 16)

    @property
    def error_code_value(self) -> int:
        return self._error_code

    @property
    def error_code(self) -> RegistryErrorCodes:
        return RegistryErrorCodes(self._error_code)

    @property
    def is_mdde_error(self) -> bool:
        """
        Return true if the error is specific to MDDE logic and runtime error
        :return:
        """
        return self._error_code not in [RegistryErrorCodes.RUNTIME_ERROR, RegistryErrorCodes.UNSPECIFIED_ERROR]


