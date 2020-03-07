from typing import Union, List, Dict


class BenchmarkResult:
    """
    Specific result values of the benchmark run
    """

    __slots__ = ['_error', '_throughput', '_nodes']

    def __init__(self,
                 throughput: float,
                 error: Union[None, str],
                 nodes: Union[None, List[Dict]]):
        """
        Constructor
        :param throughput: Total throughput calculated during the benchmark run
        :param error: Error text if there was one during the benchmark run, otherwise None
        :param nodes: Fine-grained per node-client statistics
        """
        self._error: Union[None, str] = error
        self._throughput: float = throughput
        self._nodes: Union[None, List[Dict]] = nodes

    @property
    def throughput(self) -> float:
        """
        Total throughput calculated during the benchmark run
        """
        return self._throughput

    @property
    def error(self) -> str:
        """
        Error text if there was one during the benchmark run, otherwise None
        """
        return self._error

    @property
    def nodes(self) -> Union[None, List[Dict]]:
        """
        Fine-grained per node-client statistics
        """
        return self._nodes

