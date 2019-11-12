from typing import TypeVar, Generic, Dict

from mdde.common.data import Key

Td = TypeVar('Td', contravariant=False, covariant=False)  # Data type
Tm = TypeVar('Tm', contravariant=False, covariant=False)  # Meta type


class Fragment(Generic[Tm]):
    """Container for a data fragment (only key and meta)"""
    _key: Key
    _meta: Tm

    def __init__(self, key: Key, meta: Tm = None):
        """
        Constructor
        :param key: Data key representing the key accepted by the target query engine.
        :param meta: Optional meta data required by the scenario.
        """
        if key is None:
            raise ValueError("Fragment key can't be None")

        self._key = key
        self._meta = meta

    @property
    def get_key(self) -> Key:
        """Get the fragment key"""
        return self._key

    @property
    def get_meta(self) -> Tm:
        """
        Get fragment meta data
        :return: None if no meta data is assigned by the scenario
        """
        return self._meta


class FragmentData(Fragment, Generic[Td]):
    """Container for a data fragment (including data)"""
    _data: Dict[Td]

    def __init__(self, key: Key, data: Dict[Td], meta: Tm = None):
        """
        Constructor
        :param key: Data key representing the key accepted by the target query engine
        :param data: Data retrieved from the storage. Must be a the same length as the number of tuple keys in the key.
        :param meta: Optional meta data required by the scenario.
        """
        try:
            _ = iter(data)  # duck typing check for Iterable
            k_len = len(key.get_tuple_keys)
            d_len = len(data)
            if k_len != d_len:
                raise ValueError("Fragment key length ({}) mismatch with data collection length ({})"
                                 .format(k_len, d_len))
            self._data = data
        except TypeError:  # not a collection
            k_len = len(key.get_tuple_keys)
            if len(key.tuple_keys) != 1:
                raise ValueError("Fragment key length ({}) mismatch with data collection length (1)"
                                 .format(k_len))
            self._data = {[*key.get_tuple_keys][0]: data}  # for consistency, make it a dictionary anyway

        super().__init__(key, meta)

    def get_data(self) -> Dict[Td]:
        """Get data associated with the fragment"""
        return self._data
