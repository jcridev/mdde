from . import RegistryResponse
from mdde.registry.exceptions import RegistryResponseError


class RegistryResponseHelper:
    """
    Functions commonly used when working with the objects returned from the registry
    """

    @staticmethod
    def raise_on_error(response: RegistryResponse):
        """
        Raise an exception if the the registry response returned an error
        :param response: RegistryResponse
        """
        try:
            if response.failed:
                raise RegistryResponseError(response.error if response.error is not None
                                            else 'Registry returned undefined error')
        except AttributeError as aex:
            raise TypeError('Expected response type is an instance of RegistryResponse class, '
                            'or an object containing a properties "failed" and "error"') from aex
