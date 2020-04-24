import os
from typing import Dict

import unittest
import logging
import random

from mdde.core import Environment
from mdde.agent.default.default_agent import DefaultAgent
from mdde.config import ConfigRegistry, ConfigEnvironment
from mdde.registry.tcp import RegistryClientTCP
from mdde.registry.protocol import PRegistryReadClient, PRegistryControlClient, PRegistryWriteClient
from mdde.registry.workload import EDefaultYCSBWorkload
from mdde.scenario.default import DefaultScenario


class EnvironmentTestCase(unittest.TestCase):
    REGISTRY_HOST = 'localhost'
    REGISTRY_PORT = 8942
    TEST_CONFIG_FILE = '../../debug/registry_config.yml'

    def setUp(self) -> None:
        # Init logging
        logging.basicConfig(level=logging.DEBUG,
                            format='%(asctime)-15s %(name)s - %(levelname)s - %(message)s')

    def test_initialization(self):
        # Scenario config
        agents_path = '../../debug/agents'
        mdde_config = ConfigEnvironment(agents_path)

        os.makedirs(os.path.abspath(agents_path), exist_ok=True)

        # Create Registry client
        tcp_client = RegistryClientTCP(self.REGISTRY_HOST, self.REGISTRY_PORT)
        read_client: PRegistryReadClient = tcp_client
        write_client: PRegistryWriteClient = tcp_client
        ctrl_client: PRegistryControlClient = tcp_client

        # Create agents
        config_container = ConfigRegistry()
        config_container.read(self.TEST_CONFIG_FILE)

        agents = list()
        idx = 0
        for node in config_container.get_nodes():
            agents.append(DefaultAgent(node.id, idx, node.id))
            idx += 1

        # Create scenario
        scenario = DefaultScenario(num_fragments=20,
                                   num_steps_before_bench=2,
                                   agents=agents,
                                   benchmark_clients=5,
                                   data_gen_workload=EDefaultYCSBWorkload.READ_10000_1000_LATEST,
                                   bench_workload=EDefaultYCSBWorkload.READ_10000_1000_LATEST)

        env_config = ConfigEnvironment(tmp_dir='../../debug/debug/temp',
                                       result_dir='../../debug/debug/result')

        # Create environment
        environment = Environment(config=env_config,
                                  scenario=scenario,
                                  registry_ctrl=ctrl_client,
                                  registry_write=write_client,
                                  registry_read=read_client,
                                  write_stats=True)
        # Re-generate data
        environment.initialize_registry()

        # Reset
        observations = environment.reset()

        # Retrieve observation and action spaces
        osb = environment.observation_space
        act: Dict[int, int] = environment.action_space

        # Run benchmark
        # environment.benchmark()

        # Make steps
        for i in range(0, 20):
            osb = environment.observation_space
            action_n = {}
            for k, v in act.items():
                action_n[k] = random.randrange(0, v - 1)
            obs_s, reward_s, done = environment.step(action_n=action_n)

        # Reset
        reset = environment.reset()

        # Make steps
        for i in range(0, 20):
            osb = environment.observation_space
            action_n = {}
            for k, v in act.items():
                action_n[k] = random.randrange(0, v - 1)
            obs_s, reward_s, done = environment.step(action_n=action_n)


if __name__ == '__main__':
    unittest.main()
