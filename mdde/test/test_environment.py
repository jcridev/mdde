import unittest

from mdde.core import Environment
from mdde.registry.tcp import RegistryClientTCP
from mdde.registry.protocol import PRegistryReadClient, PRegistryControlClient, PRegistryWriteClient
from mdde.scenario.default import DefaultScenario


class EnvironmentTestCase(unittest.TestCase):
    REGISTRY_HOST = 'localhost'
    REGISTRY_PORT = 8942

    def test_initialization(self):
        # Create Registry client
        tcp_client = RegistryClientTCP(self.REGISTRY_HOST, self.REGISTRY_PORT)
        read_client: PRegistryReadClient = tcp_client
        write_client: PRegistryWriteClient = tcp_client
        ctrl_client: PRegistryControlClient = tcp_client

        # Create scenario
        scenario = DefaultScenario()

        # Create environment
        environment = Environment(scenario, ctrl_client, write_client, read_client)
        # Re-generate data
        environment.initialize_registry()


if __name__ == '__main__':
    unittest.main()
