import unittest
import logging

from mdde.agent.default.default_agent import DefaultAgent
from mdde.config import ConfigRegistry
from mdde.core import Environment
from mdde.registry.tcp import RegistryClientTCP
from mdde.registry.protocol import PRegistryReadClient, PRegistryControlClient, PRegistryWriteClient
from mdde.scenario.default import DefaultScenario


class EnvironmentTestCase(unittest.TestCase):
    REGISTRY_HOST = 'localhost'
    REGISTRY_PORT = 8942
    TEST_CONFIG_FILE = '../../test/registry_config.yml'

    def setUp(self) -> None:
        # Init logging
        logging.basicConfig(level=logging.DEBUG,
                            format='%(asctime)-15s %(name)s - %(levelname)s - %(message)s')

    def test_initialization(self):
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
        scenario = DefaultScenario(100, agents)

        # Create environment
        environment = Environment(scenario, ctrl_client, write_client, read_client)
        # Re-generate data
        environment.initialize_registry()

        # Reset
        environment.reset()


if __name__ == '__main__':
    unittest.main()
