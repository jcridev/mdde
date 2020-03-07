from typing import Union, Dict, AnyStr

import yaml


class ConfigEnvironment:
    """MDDE environment configuration, by default passed to agents and scenarios."""

    __slots__ = ['_temp_dir', '_adds']

    def __init__(self, tmp_dir: str = None, **kwargs: Union[AnyStr, int, float]):
        """
        Initialize the MDDE configuration file
        :param tmp_dir: (Optional) Path to the temp directory where any files that might be required by MDDE will be
        created. It's assumed the the folder exists and writable.
        :param kwargs: Any additional custom arguments. Available for retrieval via self.get(key)
        """
        self._temp_dir: Union[None, str] = tmp_dir
        """Path to the directory where the environment should store whatever files it needs, if it needs, for the run. 
        If your scenario or agent is required to store any kind of files locally to function, these should be places in 
        the directory defined in this attribute."""

        self._adds: Union[None, Dict[str, Union[AnyStr, int, float]]] = None
        """Key value pairs that are not standard for MDDE but instead required by any custom scenarios or agents."""
        if len(kwargs) > 0:
            self._adds = kwargs

    @property
    def temp_dir(self) -> Union[None, str]:
        """Path to the directory where the environment should store whatever files it needs, if it needs, for the run.
        If your scenario or agent is required to store any kind of files locally to function, these should be places in
        the directory defined in this attribute."""
        return self._temp_dir

    def get(self, key: str) -> Union[AnyStr, int, float]:
        """Get custom attribute defined by the key. If the requested key is not found, KeyError is raised"""
        if self._adds is None:
            raise KeyError("No custom attributes is defined for the current configuration")
        return self._adds[key]


class ConfigEnvironmentYaml(ConfigEnvironment):
    """Reading MDDE config from a YAML"""

    __field_temp_dir = 'temp-dir'
    __field_adds = 'args'

    def __init__(self):
        super().__init__()

    def read(self, file_path: str) -> None:
        """Fill the object form a yml file
        :param file_path: Path to the MDDE config YAML
        """
        with open(file_path, 'r') as stream:
            yml_file = yaml.safe_load(stream)

            if self.__field_temp_dir in yml_file:
                self._temp_dir = yml_file[self.__field_temp_dir]
            if self.__field_adds in yml_file:
                self._adds = yml_file[self.__field_adds]

    def write(self, file_path: str) -> None:
        """Write the current configuration to a file
        :param file_path: Path to the MDDE config YAML
        """
        c_dict = {self.__field_temp_dir: self._temp_dir}
        if self._adds is not None:
            c_dict[self.__field_adds] = self._adds

        with open(file_path, 'w') as yaml_file:
            yaml.dump(c_dict, yaml_file, default_flow_style=False)
