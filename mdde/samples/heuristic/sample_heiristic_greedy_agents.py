import argparse
import logging

from typing import List
from pathlib import Path

import numpy as np

from mdde.core import Environment
from mdde.agent.default import SingleNodeDefaultAgent
from mdde.scenario.default import DefaultScenario
from mdde.config import ConfigRegistry, ConfigEnvironment
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.registry.tcp import RegistryClientTCP


logging.basicConfig(level=logging.DEBUG)


class MDDEGreedyAgents:
    """Each node contains each fragment. Measure the performance.

    Suitable for the Default Scenario with Single Node Default Agents.

    Note: This code is relying on using protected methods and properties. It's not an example of RL, but a mere
    validation core based on predefined (naive) heuristics."""

    # Note: in this sample Readability > Efficiency

    run_result_dir = None
    """Ray results output folder for the current experimental run."""

    mdde_registry_host = 'localhost'
    """MDDE registry host."""
    mdde_registry_port = 8942
    """MDDE registry control TCP port."""
    mdde_registry_config = None
    """Path to the MDDE registry configuration YAML."""
    env_temp_dir = None
    """Path to directory for temporary files created by the scenario or agents."""

    NUM_FRAGMENTS = 20
    NUM_BENCHMARK_CLIENTS = 25

    def run(self, config):
        # Result paths
        result_dir_path_root = Path(self.run_result_dir).resolve()

        result_dir_path_mdde_obj = result_dir_path_root.joinpath("mdde")
        result_dir_path_mdde_obj.mkdir(parents=True, exist_ok=True)
        result_dir_path_mdde = str(result_dir_path_mdde_obj)
        # Config
        config_file_full_path = str(Path(self.mdde_registry_config).resolve())
        # MDDE tmp
        temp_env_dir = self.env_temp_dir

        mdde_config = ConfigEnvironment(tmp_dir=temp_env_dir,
                                        result_dir=result_dir_path_mdde)

        def make_env(host: str,
                     port: int,
                     reg_config: str,
                     env_config: ConfigEnvironment,
                     write_stats: bool,
                     initial_benchmark: bool = False) -> Environment:
            """
            Configure MDDE environment to run default.
            :param host: MDDE registry host or IP.
            :param port: MDDE registry control port.
            :param reg_config: Path to MDDE registry config.
            :param env_config: Environment configuration object.
            :param write_stats: True to write additional analytics info.
            :param initial_benchmark: Execute benchmark immediately upon execution.
            :param do_nothing: Enable or disable the agents' "do_nothing" action.
            :return: MDDE Environment.
            """

            # Ray is peculiar in the way it handles environments, passing a pre-configured environment might cause
            # unexpected behavior. Customize the code of this extension if more complex environment are needed

            # Create Registry client
            tcp_client = RegistryClientTCP(host, port, keep_open=True)
            read_client: PRegistryReadClient = tcp_client
            write_client: PRegistryWriteClient = tcp_client
            ctrl_client: PRegistryControlClient = tcp_client

            # Registry configuration
            config_container = ConfigRegistry()
            config_container.read(reg_config)

            # Create agents
            agents = list()
            idx = 0
            for node in config_container.get_nodes():
                agents.append(SingleNodeDefaultAgent(agent_name=node.id,
                                                     agent_id=idx,
                                                     data_node_id=node.id,
                                                     write_stats=write_stats,
                                                     allow_do_nothing=True))
                idx += 1

            # Create scenario
            scenario = DefaultScenario(num_fragments=self.NUM_FRAGMENTS,
                                       num_steps_before_bench=14,
                                       agents=agents,
                                       benchmark_clients=self.NUM_BENCHMARK_CLIENTS)  # Number of YCSB threads

            # Create environment
            environment = Environment(config=env_config,
                                      scenario=scenario,
                                      registry_ctrl=ctrl_client,
                                      registry_write=write_client,
                                      registry_read=read_client,
                                      write_stats=write_stats)
            # Re-generate data
            environment.initialize_registry(with_benchmark=initial_benchmark)

            return environment

        env = make_env(host=self.mdde_registry_host,
                       port=self.mdde_registry_port,
                       reg_config=config_file_full_path,
                       env_config=mdde_config,
                       write_stats=False,
                       initial_benchmark=True)
        """Initialized instance of the environment."""

        node_id_actions = {agent.data_node_ids[0]: agent.get_actions_described() for agent in env._scenario.get_agents()}
        """Data node id as a key. Actions of the agent managing the node as values"""
        step = 0
        while True:
            # Get full initial allocation
            nodes, sorted_fragments, allocation = env._scenario.get_full_allocation_observation(
                registry_read=env._registry_read)

            act_n = self.select_actions(allocation, node_id_actions, nodes, sorted_fragments)
            if len([a for a in act_n.values() if a != 0]) == 0:
                logging.debug("All agents are doing nothing, breaking")
                break
            obs_s, reward, done, act_l_s = env.step(act_n)
            step += 1

        logging.debug("Done after %d steps", step)
        # Get described actions
        agents = env._scenario.get_agents()
        node_agents = {agent.data_node_ids[0]: agent for agent in agents}

    def select_actions(self, allocation, node_id_actions, nodes, sorted_fragments):
        act_n = {}
        for idx, node in enumerate(nodes):
            node_allocation = allocation[idx]
            not_allocated = np.where(node_allocation == 0)[0]
            if len(not_allocated) == 0:
                act_n[idx] = 0  # do-nothing
                continue  # this node already has all of the fragments

            first_not_allocated = not_allocated[0]
            # find the node that has the fragment
            source_node_idx = None
            for f_idx, foreign_node_allocation in enumerate(allocation):
                if foreign_node_allocation[first_not_allocated] == 1:
                    source_node_idx = f_idx

            not_allocated_fragment = sorted_fragments[first_not_allocated]
            if source_node_idx is None:
                raise RuntimeError("Failed to locate a fragment")

            # get the corresponding copy action
            c_actions = node_id_actions[node.node_id]
            copy_action_idx = None
            for own_act_idx, own_action in enumerate(c_actions):
                if own_action.node_source_id == nodes[source_node_idx].node_id \
                        and own_action.node_destination_id == node.node_id \
                        and own_action.fragment_id == not_allocated_fragment \
                        and own_action.is_del is False:
                    copy_action_idx = own_act_idx

            if copy_action_idx is None:
                raise RuntimeError("Failed to locate an appropriate copy action")

            act_n[idx] = copy_action_idx
        return act_n


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-r', '--result-dir',
                        help='Results dir',
                        type=str,
                        default='../../../debug/debug/result')
    parser.add_argument('-t', '--temp-dir',
                        help='Temp folder (ray temporary files)',
                        type=str,
                        default='../../../debug/debug/temp')
    parser.add_argument('--reg-host',
                        help='MDDE registry host or IP',
                        type=str,
                        default='localhost')
    parser.add_argument('--reg-port',
                        help='MDDE registry control TCP port',
                        type=int,
                        default=8942)
    parser.add_argument('--env-temp-dir',
                        help='Directory for temporary files created by the scenario or agents',
                        type=str,
                        default='../../../debug/agents')
    parser.add_argument('-c', '--config',
                        help='Path to the MDDE registry configuration YAML',
                        type=str,
                        default='../../../debug/registry_config.yml')

    config = parser.parse_args()

    MDDEGreedyAgents.run_result_dir = config.result_dir
    MDDEGreedyAgents.ray_temp_dir = config.temp_dir

    MDDEGreedyAgents.mdde_registry_host = config.reg_host
    MDDEGreedyAgents.mdde_registry_port = config.reg_port
    MDDEGreedyAgents.mdde_registry_config = config.config

    MDDEGreedyAgents.env_temp_dir = config.env_temp_dir

    runner = MDDEGreedyAgents()
    runner.run(config)
