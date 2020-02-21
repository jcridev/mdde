from typing import TypeVar, Dict

from mdde.registry.container import RegistryResponse

T = TypeVar('T')


class RegistryResponseJson(RegistryResponse[T]):
    R_RES = 'result'
    R_ERR = 'error'
    R_ERRCODE = 'errcode'

    def __init__(self, response: Dict):
        super().__init__(response[self.R_RES], response[self.R_ERR], response[self.R_ERRCODE])
