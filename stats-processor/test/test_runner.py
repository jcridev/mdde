import json
import threading
import time
import unittest
import uuid
import shutil
import tempfile

from kafka import KafkaProducer, KafkaAdminClient

from mdde_stats.config import StatsProcessorConfig
from mdde_stats.service import StatsCollectorRunner, Util
from mdde_stats.service.data import LocalDataManager


class TestKafkaProcessors(unittest.TestCase):
    def setUp(self):
        # Create a temporary directory
        self.test_dir = tempfile.mkdtemp()

    def tearDown(self):
        # Remove the directory after the test
        shutil.rmtree(self.test_dir)

    def test_runner(self):
        """
        Generalized producer consumer test.

        Assuming there is an empty test kafka rollout locally at port 9092
        """
        test_run_id = str(uuid.uuid1())

        test_config = StatsProcessorConfig()
        test_config.servers = ['localhost:9092']
        test_config.topics = ['test-topic']
        test_config.client_id = 'test-client'
        # Remove existing topics

        admin_client = KafkaAdminClient(bootstrap_servers=test_config.servers, client_id='test-admin')
        kafka_topics = admin_client.list_topics()
        existing_topics = set(test_config.topics).intersection(kafka_topics)
        if len(existing_topics) > 0:
            admin_client.delete_topics(existing_topics)


        # Initialize the database
        local_db_mngr = LocalDataManager(self.test_dir)
        local_db_mngr.initialize_db()

        runner = StatsCollectorRunner(self.test_dir, test_config)
        runner.initialize_runner()

        test_producer = TestEventsProducer(kafka_servers=test_config.servers,
                                           test_topic=test_config.topics[0],
                                           test_run_id=test_run_id)
        test_producer.start()

        runner.start()

        time.sleep(10)  # run for 10 seconds

        runner.stop()
        print('Runner stopping')
        runner.join()
        print('Runner exited')

        test_producer.stop()
        print('Producer stopping')
        test_producer.join()
        print('Producer exited')

        with local_db_mngr as bench:
            print(bench.get_run_nodes(test_run_id))

        # self.assertEqual(True, False)


class TestEventsProducer(threading.Thread):
    def __init__(self, kafka_servers, test_topic: str, test_run_id: str):
        threading.Thread.__init__(self)
        self._cancellation_token = threading.Event()
        self._test_run_id = test_run_id
        self._servers = kafka_servers
        self._topic = test_topic

    def stop(self):
        self._cancellation_token.set()

    def run(self):
        producer = KafkaProducer(bootstrap_servers=self._servers,
                                 value_serializer=lambda m: json.dumps(m).encode('utf-8'))


        test_node_id = str(uuid.uuid1())

        while not self._cancellation_token.is_set():
            msg = {
                "r": self._test_run_id,
                "n": test_node_id,
                "a": 1,
                "ts": int(time.time()),
                "t": str(uuid.uuid4()),
                "s": "unittest"
            }
            producer.send(self._topic, msg)
            time.sleep(1)

        producer.close()


if __name__ == '__main__':
    unittest.main()
