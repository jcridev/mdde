from pathlib import Path
from typing import Union, Dict, AnyStr, Any

import re
import yaml


class ConfigEnvironment:
    """MDDE environment configuration, by default passed to agents and scenarios."""

    __slots__ = ['_temp_dir', '_result_dir', '_adds']

    def __init__(self,
                 tmp_dir: str = None,
                 result_dir: str = None,
                 **kwargs: Union[AnyStr, int, float]):
        """
        Initialize the MDDE configuration file
        :param tmp_dir: (Optional) Path to the temp directory where any files that might be required by MDDE will be
        created. It's assumed the the folder exists and writable.
        :param result_dir: (Optional) Typically the rewards and learning metrics are recorded by the DRL framework, but
        if there is a need for scenario or agents to save additional output for future analysis, name of the output
        folder should be specified here.
        :param kwargs: Any additional custom arguments. Available for retrieval via self.get(key)
        """
        self._temp_dir: Union[None, str] = str(Path(tmp_dir).resolve()) if tmp_dir else None
        """Path to the directory where the environment should store whatever files it needs, if it needs, for the run. 
        If your scenario or agent is required to store any kind of files locally to function, these should be places in 
        the directory defined in this attribute."""

        self._result_dir: Union[None, Path] = Path(result_dir).resolve() if result_dir else None
        """Path to the root folder where agents or scenarios."""

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

    def result_dir(self, for_entity: Any,
                   pfx: str = '') -> str:
        """
        Generate a folder and return full path to it where a scenario or an agent can store any additional
        custom statistical data.
        :param pfx: Prefix of the folder name. For example 's_' for scenario or 'a_' for an agent.
        :param for_entity: Subclass of an ABCScenario or an ABCAgent
        :return: Full path to the folder generated for this agent or a scenario instance.
        """
        if for_entity is None:
            raise TypeError("Result dir can only be generated for an instance of a scenario or an agent")

        e_name = re.sub('[^A-Za-z0-9_]+', '-', for_entity.name)
        experiment_id = for_entity.experiment_id
        e_group = ''
        e_id = ''
        if hasattr(for_entity, 'id'):
            e_id = '_' + str(for_entity.id)
        if hasattr(for_entity, 'group'):
            e_group = '_' + for_entity.group

        path_res = self._result_dir.joinpath("{}_{}_{}{}{}".format(experiment_id, pfx, e_name, e_id, e_group))
        path_res.mkdir(parents=True, exist_ok=True)
        return str(path_res)


class ConfigEnvironmentYaml(ConfigEnvironment):
    """Reading MDDE config from a YAML"""

    FIELD_TEMP_DIR = 'temp-dir'
    FIELD_RESULT_DIR = 'result-dir'
    FIELD_ADDS = 'args'

    def __init__(self):
        super().__init__()

    def read(self, file_path: str) -> None:
        """Fill the object form a yml file
        :param file_path: Path to the MDDE config YAML
        """
        with open(file_path, 'r') as stream:
            self.loadYaml(stream)

    def loadYaml(self, yaml_obj) -> None:
        """
        Load YAML contents from a suitable source
        :param yaml_obj: String, file stream
        """
        yml_file = yaml.safe_load(yaml_obj)

        if self.FIELD_TEMP_DIR in yml_file:
            self._temp_dir = yml_file[self.FIELD_TEMP_DIR]

        if self.FIELD_RESULT_DIR in yml_file:
            self._result_dir = Path(yml_file[self.FIELD_RESULT_DIR])

        if self.FIELD_ADDS in yml_file:
            self._adds = yml_file[self.FIELD_ADDS]

    def write(self, file_path: str) -> None:
        """Write the current configuration to a file
        :param file_path: Path to the MDDE config YAML
        """
        c_dict = self.ConfigEnvironmentYamlSerializer.to_dict(self)

        with open(file_path, 'w') as yaml_file:
            yaml.dump(c_dict, yaml_file, default_flow_style=False)

    def yaml(self) -> str:
        """
        Get the config YAML as string.
        :return: YAML string with the current contents of the config.
        """
        c_dict = self.ConfigEnvironmentYamlSerializer.to_dict(self)
        return yaml.dump(c_dict)

    class ConfigEnvironmentYamlSerializer:
        @classmethod
        def to_dict(cls, config: ConfigEnvironment):
            c_dict = {ConfigEnvironmentYaml.FIELD_TEMP_DIR: config._temp_dir}

            if config._result_dir is not None:
                c_dict[ConfigEnvironmentYaml.FIELD_RESULT_DIR] = str(config._result_dir)

            if config._adds is not None:
                c_dict[ConfigEnvironmentYaml.FIELD_ADDS] = config._adds

            return c_dict

        @classmethod
        def serialize(cls, config: ConfigEnvironment) -> str:
            c_dict = cls.to_dict(config)
            return yaml.dump(c_dict)

        @classmethod
        def deserialize(cls, yaml_config: str) -> ConfigEnvironment:
            config = ConfigEnvironmentYaml()
            config.loadYaml(yaml_config)
            return config
