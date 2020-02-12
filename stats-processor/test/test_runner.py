import json
import threading
import time
import unittest

from kafka import KafkaProducer
from typing import List

from src.mddestat.config import StatsProcessorConfig
from src.mddestat.stats_runner import StatsCollectorRunner


class TestKafkaProcessors(unittest.TestCase):
    def test_runner(self):
        """
        Generalized producer consumer test.

        Assuming there is an empty test kafka rollout locally at port 9092
        """

        test_config = StatsProcessorConfig()
        test_config.servers = ['localhost:9092']
        test_config.topics = ['test-topic']
        test_config.client_id = 'test-client'

        runner = StatsCollectorRunner(test_config)
        runner.initialize_runner()

        test_producer = TestEventsProducer(kafka_servers=test_config.servers, test_topic=test_config.topics[0])
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





        #self.assertEqual(True, False)


class TestEventsProducer(threading.Thread):
    def __init__(self, kafka_servers, test_topic: str):
        threading.Thread.__init__(self)
        self._cancellation_token = threading.Event()

        self._servers = kafka_servers
        self._topic = test_topic

    def stop(self):
        self._cancellation_token.set()

    def run(self):
        producer = KafkaProducer(bootstrap_servers=self._servers,
                                 value_serializer=lambda m: json.dumps(m).encode('utf-8'))

        while not self._cancellation_token.is_set():
            producer.send(self._topic, "{'test': 'test'}")
            time.sleep(1)

        producer.close()


if __name__ == '__main__':
    unittest.main()
