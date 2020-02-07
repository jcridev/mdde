from typing import Dict

from mdde.core.exception import FragmentInstanceMissingMetaError, FragmentInstanceMissingPropertyError


class Fragment:
    def __init__(self,
                 fragment_id: str,
                 attached_properties: Dict[str, str] = None,
                 meta_values: Dict[str, str] = None):
        """
        Fragment object constructor
        :param fragment_id: Unique fragment id
        :param attached_properties: If any additional properties were at some point defined within the registry, these
        will always be returned here.
        :param meta_values: (Optional) If the fragment retrieval was requested with meta values, these must be passed
        into this container object
        """
        self._fragment_id = fragment_id
        self._fragment_properties = attached_properties
        self._meta_values = meta_values

    @property
    def fragment_id(self) -> str:
        """
        Unique identifier of the fragment
        :return:
        """
        return self._fragment_id

    @property
    def contains_meta(self) -> bool:
        """
        Check if the Fragment object contains the meta values
        :return:
        """
        return self._meta_values is not None

    def read_property(self, prop_name: str) -> str:
        """
        Retrieve a default property of a fragment
        :param prop_name:
        :return:
        """
        if not isinstance(self._fragment_properties, Dict):
            raise FragmentInstanceMissingPropertyError(self.fragment_id)
        return self._fragment_properties.get(prop_name)

    def read_meta(self, meta_name: str) -> str:
        if not self.contains_meta:
            raise FragmentInstanceMissingMetaError(self.fragment_id)
        return self._meta_values.get(meta_name)