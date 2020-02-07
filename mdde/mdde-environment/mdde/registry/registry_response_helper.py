from . import RegistryResponse
from .exceptions import RegistryResponseError


class RegistryResponseHelper:
    """
    Functions commonly used when working with the objects returned from the registry
    """

    @staticmethod
    def throw_on_error(response: RegistryResponse):
        """
        Raise an exception if the the registry response returned an error
        :param response:
        :return:
        """
        if not isinstance(response, RegistryResponse):
            raise TypeError("Expected response type is RegistryResponse")
        if response.failed:
            raise RegistryResponseError(response.error if response.error is not None
                                        else "Registry returned undefined error")
