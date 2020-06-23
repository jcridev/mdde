import argparse
import logging
import sys

from typing import List
from pathlib import Path

import numpy as np

from mdde.core import Environment
from mdde.agent.default import SingleNodeDefaultAgent
from mdde.registry.workload import EDefaultYCSBWorkload
from mdde.scenario.default import DefaultScenarioSimulation
from mdde.config import ConfigRegistry, ConfigEnvironment
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.registry.tcp import RegistryClientTCP

import abc_heuristic_sample

logging.basicConfig(level=logging.DEBUG)


class MDDERandomLegalAgents(abc_heuristic_sample.ABCMDDEHeuristicSample):
    """Agents take actions at random from the pool of the legal actions."""

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

    def run(self, config, workload):
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
            scenario = DefaultScenarioSimulation(num_fragments=self.NUM_FRAGMENTS,
                                                 num_steps_before_bench=config.bench_psteps,
                                                 agents=agents,
                                                 data_gen_workload=workload,
                                                 bench_workload=workload,
                                                 benchmark_clients=config.bench_clients,
                                                 write_stats=write_stats)  # Number of YCSB threads

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
                       write_stats=True,
                       initial_benchmark=True)
        """Initialized instance of the environment."""

        episode = 0
        while episode < config.num_episodes:
            episode += 1
            step = 0
            obs_s, act_l_s = env.observation_space
            while step < config.ep_len:
                act_n = {}
                for agent_id, agent_legal_act in act_l_s.items():
                    legal_act_indexes = np.where(agent_legal_act == 1)[0]
                    act_n[agent_id] = np.random.choice(legal_act_indexes, 1, replace=True)[0]
                obs_s, reward, done, act_l_s = env.step(act_n)

                for idx_r, agent_reward in reward.items():
                    logging.info("Reward at step {} for agent {}: {}".format(step, idx_r, agent_reward))
                logging.info("Sum of rewards: %d", sum(reward.values()))

                # self.tune_estimations(step_num=step, env=env)
                step += 1

            obs_s, act_l_s = env.reset()

        # self.out_final_results()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-r', '--result-dir',
                        help='Results dir',
                        type=str,
                        default='../../../debug/debug/result/random_agent')
    parser.add_argument('-t', '--temp-dir',
                        help='Temp folder (ray temporary files)',
                        type=str,
                        default='../../../debug/debug/temp/random_agent')
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

    parser.add_argument('--bench-psteps',
                        help='Frequency of benchmark execution (execute every N steps).',
                        type=int,
                        default=1)

    parser.add_argument('--bench-clients',
                        help='Number of benchmark clients.',
                        type=int,
                        default=50)

    parser.add_argument('--light',
                        help='Execute corresponding "light" workload.',
                        action='store_true')

    parser.add_argument('--out-file',
                        help='Dump all output to the specificed file',
                        type=str,
                        default=None)

    parser.add_argument('--num_episodes',
                        help='Total number of episodes.',
                        type=int,
                        default=1000)
    parser.add_argument('--ep_len',
                        help='Number of steps per episode.',
                        type=int,
                        default=1001)

    config = parser.parse_args()

    if config.out_file:
        fileHandler = logging.FileHandler(config.out_file)
        logging.getLogger().addHandler(fileHandler)

    MDDERandomLegalAgents.run_result_dir = config.result_dir
    MDDERandomLegalAgents.ray_temp_dir = config.temp_dir

    MDDERandomLegalAgents.mdde_registry_host = config.reg_host
    MDDERandomLegalAgents.mdde_registry_port = config.reg_port
    MDDERandomLegalAgents.mdde_registry_config = config.config

    MDDERandomLegalAgents.env_temp_dir = config.env_temp_dir

    workload = EDefaultYCSBWorkload.READ_10000_100000_LATEST_LARGE
    if config.light:
        workload = EDefaultYCSBWorkload.READ_10000_100000_LATEST

    runner = MDDERandomLegalAgents()
    runner.run(config, workload)
