from typing import TypeVar, Generic

Tk = TypeVar('Tk', contravariant=False, covariant=False)  # Key type
Td = TypeVar('Td', contravariant=False, covariant=False)  # Data type
Tm = TypeVar('Tm', contravariant=False, covariant=False)  # Meta type


class Fragment(Generic[Tk, Tm]):
    """Container for a data fragment (only key and meta)"""
    _key: Tk
    _meta: Tm

    def __init__(self, key: Tk, meta: Tm = None):
        """
        Constructor
        :param key: Data key representing the key accepted by the target query engine
        :param meta: Meta data required by the
        """
        if key is None:
            raise ValueError("Fragment key can't be None")

        self._key = key
        self._meta = meta


class FragmentData(Fragment, Generic[Td]):
    """Container for a data fragment (including data)"""
    _data: Td

    def __init__(self, key: Tk, data: Td, meta: Tm = None):
        """
        Constructor
        :param key: Data key representing the key accepted by the target query engine
        :param data: Data retrieved from the storage
        :param meta: Meta data required by the
        """
        super().__init__(key, meta)
        self._data = data

