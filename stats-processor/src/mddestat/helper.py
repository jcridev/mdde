import sys
from collections import Sequence


class Helper:
    """
    Common methods used across the application but not  falling into any specific category
    """
    @staticmethod
    def is_sequence_not_string(obj: object) -> bool:
        return isinstance(obj, Sequence) and not isinstance(obj, (str, bytes, bytearray))

