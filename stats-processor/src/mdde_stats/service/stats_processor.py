import json
import multiprocessing
import sys
from typing import List
from kafka.admin import KafkaAdminClient, NewTopic
from kafka import KafkaConsumer
import traceback

from mdde_stats.service import Util
from mdde_stats.service.data import LogEvent
from mdde_stats.service.data.manager import LocalDataManager


class StatsProcessor(multiprocessing.Process):
    """
    Listens to the incoming MDDE events and catalogs them
    """
    def __init__(self,
                 local_data_folder: str,
                 kafka_servers: List[str],
                 topics: List[str],
                 client_id: str = None):
        multiprocessing.Process.__init__(self)
        self._cancellation_token = multiprocessing.Event()

        if kafka_servers is None:
            raise TypeError('kafka_servers can\'t be None')

        if topics is None:
            raise TypeError('topics can\'t be None')
        self._local_data_folder = local_data_folder
        self._servers = kafka_servers
        self._topics = topics

        if client_id is not None:
            self._client_id = client_id
        else:
            import uuid
            self._client_id = str(uuid.uuid1()).replace('-', '')

    def stop(self):
        self._cancellation_token.set()

    def run(self):
        self._listen()

    @property
    def client_id(self) -> str:
        return self._client_id

    def initialize_kafka_topics(self):
        """
        Make sure the server connection is possible and the tracked topics exist
        """
        admin_client = KafkaAdminClient(bootstrap_servers=self._servers, client_id=self._client_id)
        existing_topics = admin_client.list_topics()
        if Util.is_sequence_not_string(self._topics):
            non_existing_topics = set(self._topics).difference(existing_topics)
            if len(non_existing_topics) > 0:
                topic_list = []
                for topic_name in non_existing_topics:
                    #  TODO: Parameterize partitions and replication factor for future scalability
                    topic_list.append(NewTopic(name=topic_name, num_partitions=1, replication_factor=1))
                admin_client.create_topics(new_topics=topic_list, validate_only=False)

    def _listen(self):
        """
        Listen for the assigned topics
        """
        consumer = KafkaConsumer(client_id=self._client_id,
                                 auto_offset_reset='earliest',
                                 group_id='mdde-group-c',
                                 fetch_max_wait_ms=500,
                                 value_deserializer=self._relaxed_json_msg_deserializer)
        consumer.subscribe(self._topics)

        local_data_manager = LocalDataManager(self._local_data_folder)
        with local_data_manager as local_bench_data:
            while not self._cancellation_token.is_set():
                for message in consumer:
                    try:
                        event = LogEvent()
                        event.from_json_obj(message.value)
                        local_bench_data.insert_event(event)
                        local_bench_data.commit()  # TODO: Not as frequent commits or batch insertions
                    except:
                        print('Error processing incoming stats message: %s', traceback.format_exc(), file=sys.stderr)
                    if self._cancellation_token.is_set():
                        break

            consumer.close()

    @staticmethod
    def _relaxed_json_msg_deserializer(value):
        try:
            return json.loads(value.decode('utf-8'))
        except json.decoder.JSONDecodeError:
            traceback.print_exc()
            print('Unable to decode: %s', value, file=sys.stderr)
            return None
