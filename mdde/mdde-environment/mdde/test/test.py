from mdde.registry.tcp import RegistryClientTCP


def test_client_run():
    tcp_client = RegistryClientTCP("localhost", 8942)
    print(tcp_client.ctrl_set_benchmark_mode().error)


test_client_run()
