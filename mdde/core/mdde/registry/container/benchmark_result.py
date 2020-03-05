from typing import Union, List, Dict


class BenchmarkResult:

    __slots__ = ['_error', '_throughput', '_nodes']

    def __init__(self,
                 throughput: float,
                 error: Union[None, str],
                 nodes: Union[None, List[Dict]]):
        self._error: Union[None, str] = error
        self._throughput: float = throughput
        self._nodes: Union[None, List[Dict]] = nodes

    @property
    def throughput(self) -> float:
        return self._throughput

    @property
    def error(self) -> str:
        return self._error

    @property
    def nodes(self) -> Union[None, List[Dict]]:
        return self._nodes

