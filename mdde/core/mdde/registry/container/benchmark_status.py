from typing import Union

from mdde.registry.container import BenchmarkResult
from mdde.registry.enums import EBenchmarkState


class BenchmarkStatus:
    """
    Response to the benchmark status request
    """

    __slots__ = ['_stage', '_run_id', '_failed', '_completed', '_result']

    def __init__(self,
                 stage: Union[None, EBenchmarkState],
                 run_id: str,
                 failed: bool,
                 completed: bool,
                 result: Union[None, BenchmarkResult]):
        self._stage: Union[None, EBenchmarkState] = stage
        self._run_id: str = run_id
        self._failed: bool = failed
        self._completed: bool = completed
        self._result: Union[None, BenchmarkResult] = result

    @property
    def completed(self) -> bool:
        """If true, the latest benchmark run was finished (success or failure state achieved)."""
        return self._completed

    @property
    def failed(self) -> bool:
        """If true, the latest benchmark run failed."""
        return self._failed

    @property
    def run_id(self) -> str:
        """ID of the latest benchmark run."""
        return self._run_id

    @property
    def stage(self) -> Union[None, EBenchmarkState]:
        """
        Stages unknown by the client are processed as None by default.
        :return: EBenchmarkState value or None.
        """
        return self._stage

    @property
    def result(self) -> Union[None, BenchmarkResult]:
        """Results of the latest completed, successful benchmark run (data record access stats, throughput)."""
        return self._result
