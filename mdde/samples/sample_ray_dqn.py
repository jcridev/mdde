import os
import argparse

import ray
from ray import utils
from ray.rllib.agents.dqn import DQNTrainer
from ray.tune import run_experiments
from ray.tune.registry import register_env

from mdde.agent.default import SingleNodeDefaultAgent
from mdde.core import Environment
from mdde.integration.ray.ray_multiagent_env import MddeMultiAgentEnv
from mdde.config import ConfigEnvironment, ConfigRegistry
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.registry.tcp import RegistryClientTCP


# https://ray.readthedocs.io/en/latest/installation.html
from mdde.scenario.default import DefaultScenario


class DQNTestSample:
    ray_result_dir = None
    """Ray results output folder"""
    ray_temp_dir = None
    """Make sure "TEST_TEMP_DIR" not too long for the plasma store, otherwise ray will fail"""
    mdde_registry_host = 'localhost'
    """MDDE registry host"""
    mdde_registry_port = 8942
    """MDDE registry control TCP port"""
    mdde_registry_config = None
    """Path to the MDDE registry configuration YAML"""
    env_temp_dir = None
    """Path to directory for temporary files created by the scenario or agents"""

    NUM_EPISODES = 1000
    EPISODE_LEN = 201
    LEARNING_RATE = 1e-2
    NUM_ADVERSARIES = 0
    SAMPLE_BATCH_SIZE = 25
    TRAIN_BATCH_SIZE = 100

    def setUp(self) -> None:
        os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'  # {'0': 'DEBUG', '1': 'INFO', '2': 'WARNING', '3': 'ERROR'}

    def test_dqn(self):
        temp_dir_full_path = os.path.realpath(self.ray_temp_dir)
        result_dur_full_path = os.path.realpath(self.ray_result_dir)
        config_file_full_path = os.path.realpath(self.mdde_registry_config)
        temp_env_dir = os.path.realpath(self.env_temp_dir)

        os.makedirs(os.path.abspath(temp_env_dir), exist_ok=True)

        ray.init(redis_max_memory=int(ray.utils.get_system_memory() * 0.4),
                 memory=int(ray.utils.get_system_memory() * 0.2),
                 object_store_memory=int(ray.utils.get_system_memory() * 0.2),
                 num_gpus=0,
                 num_cpus=4,
                 temp_dir=temp_dir_full_path)

        mdde_config = ConfigEnvironment(temp_env_dir)

        def make_env(host: str,
                     port: int,
                     reg_config: str,
                     env_config: ConfigEnvironment,
                     write_stats: bool) -> Environment:
            """
            Configure MDDE environment to run default.
            :param host: MDDE registry host or IP.
            :param port: MDDE registry control port.
            :param reg_config: Path to MDDE registry config.
            :param env_config: Environment configuration object.
            :param write_stats: True to write additional analytics info.
            :return: MDDE Environment.
            """

            # Ray is peculiar in the way it handles environments, passing a pre-configured environment might cause
            # unexpected behavior. Customize the code of this extension if more complex environment are needed

            # Create Registry client
            tcp_client = RegistryClientTCP(host, port)
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
                agents.append(SingleNodeDefaultAgent(node.id, idx, node.id))
                idx += 1

            # Create scenario
            scenario = DefaultScenario(num_fragments=20,
                                       num_steps_before_bench=25,
                                       agents=agents,
                                       benchmark_clients=5)  # Number of YCSB threads

            # Create environment
            environment = Environment(config=env_config,
                                      scenario=scenario,
                                      registry_ctrl=ctrl_client,
                                      registry_write=write_client,
                                      registry_read=read_client,
                                      write_stats=write_stats)
            # Re-generate data
            environment.initialize_registry()

            return environment

        # Create and initialize environment before passing it to Ray
        # This makes it impossible to run multiple instances of the environment, however it's intentional due to the
        # the nature of the environment that's represented as a distributed infrastructure of services, it can't be
        # easily created and destroyed as a simple local game-like environment
        env_instance = MddeMultiAgentEnv(make_env(host=self.mdde_registry_host,
                                                  port=self.mdde_registry_port,
                                                  reg_config=config_file_full_path,
                                                  env_config=mdde_config,
                                                  write_stats=False))

        def env_creator(kvargs):
            env = make_env(**kvargs)
            return MddeMultiAgentEnv(env)

        register_env("mdde", env_creator)

        # generate policies based on the created environment instance
        def gen_policy(i):
            return (
                None,
                env_instance.observation_space_dict[i],
                env_instance.action_space_dict[i],
                {
                    "agent_id": i,
                    "use_local_critic": False,
                    "obs_space_dict": env_instance.observation_space_dict[i],
                    "act_space_dict": env_instance.action_space_dict[i],
                }
            )

        policies = {"policy_%d" % i: gen_policy(i) for i in env_instance.action_space_dict.keys()}
        policy_ids = list(policies.keys())

        def policy_mapping_fn(agent_id):
            return policy_ids[agent_id]

        exp_name = "DQN_MDDE_DEBUG"

        trainer = DQNTrainer

        run_experiments({
            exp_name: {
                "run": trainer,
                "env": "mdde",
                "stop": {
                    "episodes_total": self.NUM_EPISODES,
                },
                "checkpoint_freq": 0,
                "local_dir": result_dur_full_path,
                "restore": False,
                "config": {
                    # === Log ===
                    "log_level": "ERROR",

                    # === Environment ===
                    "env_config": {
                        "host": self.mdde_registry_host,
                        "port": self.mdde_registry_port,
                        "reg_config": config_file_full_path,
                        "env_config": mdde_config,
                        "write_stats": True
                    },
                    "num_envs_per_worker": 1,
                    "horizon": self.EPISODE_LEN,

                    # === Policy Config ===
                    # --- Model ---
                    "n_step": 1,
                    "gamma": 0.95,

                    # --- Exploration ---
                    # "tau": 0.01,

                    # --- Replay buffer ---
                    "buffer_size": 10000,

                    # --- Optimization ---
                    "learning_starts": self.TRAIN_BATCH_SIZE * self.EPISODE_LEN,
                    "sample_batch_size": self.SAMPLE_BATCH_SIZE,
                    "train_batch_size": self.TRAIN_BATCH_SIZE,
                    "batch_mode": "truncate_episodes",

                    # --- Parallelism ---
                    "num_workers": 0,
                    "num_gpus": 0,
                    "num_gpus_per_worker": 0,

                    # === Multi-agent setting ===
                    "multiagent": {
                        "policies": policies,
                        "policy_mapping_fn": ray.tune.function(policy_mapping_fn)
                    },
                },
            },
        }, verbose=0, reuse_actors=False)  # reuse_actors=True - messes up the results


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-r', '--result-dir',
                        help='Results dir (tensorboard)',
                        type=str,
                        default='../../debug/debug/result')
    parser.add_argument('-t', '--temp-dir',
                        help='Temp folder (ray temporary files)',
                        type=str,
                        default='../../debug/debug/temp')
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
                        default='../../debug/agents')
    parser.add_argument('-c', '--config',
                        help='Path to the MDDE registry configuration YAML',
                        type=str,
                        default='../../debug/registry_config.yml')

    config = parser.parse_args()

    DQNTestSample.ray_result_dir = config.result_dir
    DQNTestSample.ray_temp_dir = config.temp_dir

    DQNTestSample.mdde_registry_host = config.reg_host
    DQNTestSample.mdde_registry_port = config.reg_port
    DQNTestSample.mdde_registry_config = config.config

    DQNTestSample.env_temp_dir = config.env_temp_dir

    runner = DQNTestSample()
    runner.setUp()
    runner.test_dqn()
