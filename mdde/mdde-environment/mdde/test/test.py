from mdde.registry.tcp.RegistryClientTCP import RegistryClientTCP


def testClientRun():
    tcpClient = RegistryClientTCP("localhost", 8942)
    print(tcpClient.ctrl_set_benchmark_mode().error)


testClientRun()
