from mdde_stats.service import StatsProcessor, ControlProcessor
from mdde_stats.config import StatsProcessorConfig


class StatsCollectorRunner:

    def __init__(self, data_folder: str, config: StatsProcessorConfig):
        if config is None:
            raise TypeError("configuration is not set")
        self._config = config
        self._processes = None
        self._data_folder = data_folder

    def initialize_runner(self):
        config = self._config
        stats_p = StatsProcessor(self._data_folder, config.servers, config.topics, config.client_id)
        stats_p.initialize_kafka_topics()
        control_p = ControlProcessor(config.servers, config.request_topic, config.response_topic, config.client_id)
        control_p.initialize_kafka_topics()
        self._processes = [stats_p, control_p]

    def start(self):
        if self._processes is None:
            raise ValueError('Runner is not in the initialized state.')

        for p in self._processes:
            p.start()

    def join(self):
        if self._processes is None:
            raise ValueError('Runner is not in the initialized state.')

        for p in self._processes:
            p.join()

    def stop(self):
        if self._processes is None:
            raise ValueError('Runner is not in the initialized state.')

        for p in self._processes:
            p.stop()
