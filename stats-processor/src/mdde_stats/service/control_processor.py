from typing import List
import multiprocessing

from kafka import KafkaConsumer, KafkaAdminClient
from kafka.admin import NewTopic


class ControlProcessor(multiprocessing.Process):
    _stats_request_cmd = 'stats'
    _stats_not_ready_response = 'not_ready'
    _unknown_cmd_response = 'unrecognized_request'

    def __init__(self, kafka_servers: List[str], request_topic: str, response_topic: str, client_id: str = None):
        multiprocessing.Process.__init__(self)
        self._cancellation_token = multiprocessing.Event()

        if kafka_servers is None:
            raise TypeError('kafka_servers can\'t be None')

        self._servers = kafka_servers
        self._request_topic = request_topic
        self._response_topic = response_topic

        if client_id is not None:
            self._client_id = client_id
        else:
            import uuid
            self._client_id = str(uuid.uuid1()).replace('-', '')

    @property
    def client_id(self) -> str:
        return self._client_id

    def initialize_kafka_topics(self):
        """
        Make sure the server connection is possible and the tracked topics exist
        """
        admin_client = KafkaAdminClient(bootstrap_servers=self._servers, client_id=self._client_id)
        existing_topics = admin_client.list_topics()
        if self._request_topic not in existing_topics:
            request_topic = NewTopic(name=self._request_topic, num_partitions=1, replication_factor=1)
            admin_client.create_topics(new_topics=[request_topic], validate_only=False)

        if self._response_topic not in existing_topics:
            response_topic = NewTopic(name=self._response_topic, num_partitions=1, replication_factor=1)
            admin_client.create_topics(new_topics=[response_topic], validate_only=False)

    def stop(self):
        self._cancellation_token.set()

    def run(self):
        consumer = KafkaConsumer(bootstrap_servers=self._servers,
                                 auto_offset_reset='earliest',
                                 group_id='mdde-group-c',
                                 client_id=self._client_id,
                                 fetch_max_wait_ms=500,
                                 consumer_timeout_ms=1000)
        consumer.subscribe([self._request_topic])
        last_offset = None
        while not self._cancellation_token.is_set():
            for message in consumer:
                print(message)
                last_offset = message.offset
                if self._cancellation_token.is_set():
                    break

        consumer.close()
