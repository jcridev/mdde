from enum import Enum


class ELogAction(Enum):
    """
    Types of actions that can be processed by the statistics processor
    """
    UNKNOWN = 0
    READ_TUPLE = 1
