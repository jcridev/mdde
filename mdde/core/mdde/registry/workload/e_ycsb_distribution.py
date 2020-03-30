from enum import Enum


class EYCSBWorkloadRequestDistribution(Enum):
    """Types of request distributions during the workload run in YCSB"""
    ZIPFIAN = 'zipfian'
    UNIFORM = 'uniform'
    LATEST = 'latest'
