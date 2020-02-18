from typing import List
import configparser


class StatsProcessorConfig:
    _kafka_section = 'kafka'
    _kafka_servers_value = 'servers'
    _kafka_topics_value = 'topics'
    _kafka_ctrl_topic_value = 'topic-ctrl'
    _kafka_ctrl_response_topic_value = 'topic-response'
    _kafka_client_id_value = 'client-id'
    _service_section_value = 'service'
    _data_dir_value = 'dir'

    def __init__(self):
        self._topics = None
        self._client_id = None
        self._servers = None
        self._data_dir = None
        self._ctrl_topic = 'mdde-stats-ctrl'
        self._ctrl_response_topic = 'mdde-stats-response'

    def read(self, file: str):
        """
        Read configuration file and fill in object values
        :param file: Full path to the configuration file
        """
        config = configparser.ConfigParser()
        config.read(file)
        self.client_id = config[StatsProcessorConfig._kafka_section][StatsProcessorConfig._kafka_client_id_value]
        self.data_dir = config[StatsProcessorConfig._service_section_value][StatsProcessorConfig._data_dir_value]

        servers_str = config[StatsProcessorConfig._kafka_section][StatsProcessorConfig._kafka_servers_value]
        self.servers = servers_str.split(',')

        topics_str = config[StatsProcessorConfig._kafka_section][StatsProcessorConfig._kafka_topics_value]
        self.topics = topics_str.split(',')

    def save(self, file: str):
        """
        Save current configuration to file
        :param file: Path to file (will be overridden)
        """
        config = configparser.ConfigParser()
        config[StatsProcessorConfig._kafka_section][StatsProcessorConfig._kafka_client_id_value] = self.client_id
        config[StatsProcessorConfig._service_section_value][StatsProcessorConfig._data_dir_value] = self.data_dir

        if self.servers is not None:
            servers_str = ','.join(self.servers)
        else:
            servers_str = ''
        config[StatsProcessorConfig._kafka_section][StatsProcessorConfig._kafka_servers_value] = servers_str

        if self.topics is not None:
            topics_str = ','.join(self.topics)
        else:
            topics_str = ''
        config[StatsProcessorConfig._kafka_section][StatsProcessorConfig._kafka_topics_value] = topics_str

        with open(file, 'w') as configfile:
            config.write(configfile)

    @property
    def servers(self) -> List[str]:
        return self._servers

    @servers.setter
    def servers(self, value: List[str]):
        self._servers = value

    @property
    def topics(self) -> List[str]:
        return self._topics

    @topics.setter
    def topics(self, value: List[str]):
        self._topics = value

    @property
    def client_id(self):
        return self._client_id

    @client_id.setter
    def client_id(self, value: str):
        if value is None:
            raise TypeError('Client id must be a valid string')
        value = value.strip()
        if value == '':
            raise ValueError('Client id can\'t be an empty string or consists out of only whitespaces')
        self._client_id = value

    @property
    def data_dir(self):
        return self._data_dir

    @data_dir.setter
    def data_dir(self, value: str):
        self._data_dir = value

    @property
    def request_topic(self) -> str:
        return self._ctrl_topic

    @request_topic.setter
    def request_topic(self, value: str):
        self._ctrl_topic = value

    @property
    def response_topic(self) -> str:
        return self._ctrl_response_topic

    @response_topic.setter
    def response_topic(self, value: str):
        self._ctrl_response_topic = value
