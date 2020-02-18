from enum import Enum


class ERunState(Enum):
    """
    Types of benchmark data collection run state
    """
    UNKNOWN = 0
    STARTED = 1
    FINISHED = 2
    PROCESSED = 3
