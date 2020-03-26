import unittest

from mdde.config import ConfigRegistry


class ConfigTestCase(unittest.TestCase):
    TEST_CONFIG_FILE = '../../debug/registry_config.yml'

    def test_initialization(self):
        config_container = ConfigRegistry()
        config_container.read(self.TEST_CONFIG_FILE)

        for node in config_container.get_nodes():
            print("{} | {}".format(node.id, node.default))


if __name__ == '__main__':
    unittest.main()
