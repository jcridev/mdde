from typing import TypeVar, Generic, Set, FrozenSet, Callable

Tk = TypeVar('Tk', contravariant=False, covariant=False)  # Key type


class Key(Generic[Tk]):
    """
    Fragment key. Fragment can contain multiple tuples so
    """
    _key: int
    _tupleKeys: frozenset

    def __init__(self, key: Set[Tk], c_hash: Callable = None):
        """
        Constructor
        :param key: keys of tuples contained in the fragment
        :param c_hash: Optional custom hash function (can be used if the data set key hashing results in collisions)
        """
        if key is None:
            raise ValueError("Fragment key can't be None")

        u_hash = c_hash
        if u_hash is None:
            u_hash = hash
        try:  # check if passed key is iterable
            _ = iter(key)  # duck typing check for Iterable

            self._tupleKeys = frozenset(key)
            self._key = u_hash(self._tupleKeys)
        except TypeError:  # not a collection
            self._tupleKeys = frozenset([key])
            if isinstance(key, int):  # support passing a singular int value
                self._key = key  # use it as a key
            else:
                self._key = u_hash(self._tupleKeys)

    @property
    def get_key(self) -> int:
        """Fragment key"""
        return self._key

    @property
    def get_tuple_keys(self) -> FrozenSet[Tk]:
        """Keys of the tuples which are part of the fragment"""
        return self._tupleKeys

    def rehash(self, c_hash: Callable) -> int:
        """
        Regenerate the fragment key using the specified hash function

        :returns: new key
        """
        new_key = c_hash(self._tupleKeys)
        return new_key

    def __eq__(self, other):
        return self.get_key == other.get_key

    def __hash__(self):
        """Key of the fragment is it's hash"""
        return self._key
