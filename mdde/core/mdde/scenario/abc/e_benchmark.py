from enum import IntEnum


class EBenchmark(IntEnum):
    """Types of benchmark to execute"""
    NO_BENCHMARK = 0
    """Execute no benchmark at all"""
    DEFAULT = 1
    """Run normal benchmark"""
    COUNTERFEIT = 2
    """Run estimation based on the previous benchmark statistics"""
