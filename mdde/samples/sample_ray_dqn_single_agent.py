import os
import argparse

from pathlib import Path

import ray
from ray import air
from ray import tune
from ray.air import CheckpointConfig
from ray.rllib.algorithms.dqn import dqn
from ray.tune import run_experiments
from ray.tune.registry import register_env

from mdde.core import Environment
from mdde.agent.default import DefaultAgent
from mdde.registry.workload import EDefaultYCSBWorkload
from mdde.scenario.default import DefaultScenario, DefaultScenarioSimulation
from mdde.config import ConfigRegistry, ConfigEnvironment
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.registry.tcp import RegistryClientTCP
from mdde.integration.ray import MddeSignleAgentEnv


# https://ray.readthedocs.io/en/latest/installation.html


class DQNTestSample:
    """Demonstration sample code showing how RAY's DQN can be executed used with MDDE."""

    run_result_dir = None
    """Ray results output folder for the current experimental run."""
    ray_temp_dir = None
    """Make sure "TEST_TEMP_DIR" not too long for the plasma store, otherwise ray will fail."""
    mdde_registry_host = 'localhost'
    """MDDE registry host."""
    mdde_registry_port = 8942
    """MDDE registry control TCP port."""
    mdde_registry_config = None
    """Path to the MDDE registry configuration YAML."""
    env_temp_dir = None
    """Path to directory for temporary files created by the scenario or agents."""

    LEARNING_RATE = 1e-2
    NUM_ADVERSARIES = 0
    TRAIN_BATCH_SIZE = 100

    def setUp(self) -> None:
        os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'  # {'0': 'DEBUG', '1': 'INFO', '2': 'WARNING', '3': 'ERROR'}

    def run_dqn(self, config):
        # Workload
        selected_workload: EDefaultYCSBWorkload = EDefaultYCSBWorkload.READ_10000_100000_LATEST_LARGE
        if config.light:
            selected_workload: EDefaultYCSBWorkload = EDefaultYCSBWorkload.READ_10000_100000_LATEST

        # RAY tmp
        temp_dir_full_path_obj = Path(self.ray_temp_dir).resolve()
        temp_dir_full_path_obj.mkdir(parents=True, exist_ok=True)
        temp_dir_full_path = str(temp_dir_full_path_obj)
        # Result paths
        result_dir_path_root = Path(self.run_result_dir).resolve()
        # Separate MDDE output and Ray output
        result_dir_path_ray_obj = result_dir_path_root.joinpath("ray")
        result_dir_path_ray_obj.mkdir(parents=True, exist_ok=True)
        result_dir_path_ray = str(result_dir_path_ray_obj)
        result_dir_path_mdde_obj = result_dir_path_root.joinpath("mdde")
        result_dir_path_mdde_obj.mkdir(parents=True, exist_ok=True)
        result_dir_path_mdde = str(result_dir_path_mdde_obj)
        # Config
        config_file_full_path = str(Path(self.mdde_registry_config).resolve())
        # MDDE tmp
        temp_env_dir = self.env_temp_dir

        os.makedirs(os.path.abspath(temp_env_dir), exist_ok=True)

        ray.init(num_gpus=0,
                 num_cpus=4,
                 #temp_dir=temp_dir_full_path
                 )

        mdde_config = ConfigEnvironment(tmp_dir=temp_env_dir,
                                        result_dir=result_dir_path_mdde)

        def make_env(host: str,
                     port: int,
                     reg_config: str,
                     env_config: ConfigEnvironment,
                     write_stats: bool,
                     initial_benchmark: bool = False,
                     do_nothing: bool = True) -> Environment:
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
            node_ids = [node.id for node in config_container.get_nodes()]
            agents.append(DefaultAgent(agent_name='def_agent',
                                       agent_id=idx,
                                       data_node_ids=node_ids,
                                       write_stats=write_stats,
                                       allow_do_nothing=do_nothing))

            num_fragments: int = config.n_frags
            # Create scenario
            if config.sim:
                scenario = DefaultScenarioSimulation(num_fragments=num_fragments,
                                                     num_steps_before_bench=config.bench_psteps,
                                                     agents=agents,
                                                     benchmark_clients=config.bench_clients,
                                                     data_gen_workload=selected_workload,
                                                     bench_workload=selected_workload,
                                                     write_stats=write_stats,
                                                     ignore_conflicting_actions=config.ok_conf_a)  # Number of YCSB threads
            else:
                scenario = DefaultScenario(num_fragments=num_fragments,
                                           num_steps_before_bench=config.bench_psteps,
                                           agents=agents,
                                           benchmark_clients=config.bench_clients,
                                           data_gen_workload=selected_workload,
                                           bench_workload=selected_workload,
                                           write_stats=write_stats,
                                           ignore_conflicting_actions=config.ok_conf_a)  # Number of YCSB threads
            scenario = DefaultScenario(num_fragments=20,
                                       num_steps_before_bench=config.bench_psteps,
                                       agents=agents,
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

        def obs_shaper_2d_box(obs):
            """Reshapes the environment into a form suitable for 2D box. Example 1.
            Note: Guaranteed to work only with the Default agent - Default scenario combination."""
            # Resulted shape (Example for default scenario and default single-node agent: 2 agents, 5 fragments):
            # a_1: [0-4(allocation) 5-9(popularity) 10-14(ownership binary flag)]
            # a_2: [0-4(allocation) 5-9(popularity) 10-14(ownership binary flag)]
            # Hint: 2D array where rows are agents, and attributes in columns are as shown above.
            return obs.reshape((obs.shape[0], obs.shape[1] * obs.shape[2]), order='F')

        def obs_shaper_flat_box(obs):
            """Reshapes the environment into a form suitable for 2D 'flat' box. Example 2.
            Note: Guaranteed to work only with the Default agent - Default scenario combination."""
            # Resulted shape (Example for default scenario and default single-node agent: 2 agents, 5 fragments):
            # [0-4(a_1: allocation) 5-9(a_1: popularity) 10-14(a_1: ownership binary flag)
            #  15-19(a_2: allocation) 20-24(a_2: popularity) 25-29(a_2: ownership binary flag)]
            return obs.reshape((obs.shape[0], obs.shape[1] * obs.shape[2]), order='F') \
                .reshape((obs.shape[0] * obs.shape[1] * obs.shape[2]), order='C')

        sample_selected_shaper = obs_shaper_2d_box
        """Observation shaper selected. Set None if you want to use the default one in the wrapper."""

        # Create and initialize environment before passing it to Ray
        # This makes it impossible to run multiple instances of the environment, however it's intentional due to the
        # the nature of the environment that's represented as a distributed infrastructure of services, it can't be
        # easily created and destroyed as a simple local game-like environment
        env_instance = MddeSignleAgentEnv(env=make_env(host=self.mdde_registry_host,
                                                       port=self.mdde_registry_port,
                                                       reg_config=config_file_full_path,
                                                       env_config=mdde_config,
                                                       write_stats=False,
                                                       initial_benchmark=False,
                                                       do_nothing=config.do_nothing),
                                          observation_shaper=sample_selected_shaper)

        def env_creator(kvargs):
            env = make_env(**kvargs)
            return MddeSignleAgentEnv(env=env,
                                      observation_shaper=sample_selected_shaper)

        register_env("mdde_s", env_creator)

        dqn_config = (
            dqn.DQNConfig()
            .environment(
                env='mdde_s',
                env_config={
                    "host": self.mdde_registry_host,
                    "port": self.mdde_registry_port,
                    "reg_config": config_file_full_path,
                    "env_config": mdde_config,
                    "write_stats": True,
                    "do_nothing": config.do_nothing
                },
            )
        )

        exp_name = "DQN_MDDE_SINGLE"
        # exp_config = {
        #     # === Log ===
        #     "log_level": "ERROR",
        #
        #     # === Environment ===
        #     "env_config": {
        #         "host": self.mdde_registry_host,
        #         "port": self.mdde_registry_port,
        #         "reg_config": config_file_full_path,
        #         "env_config": mdde_config,
        #         "write_stats": True,
        #         "do_nothing": config.do_nothing
        #     },
        #     "num_envs_per_worker": 1,
        #     "horizon": config.ep_len,
        #
        #     # === Policy Config ===
        #     # --- Model ---
        #     "n_step": 1,
        #     # "gamma": config.gamma,
        #
        #     # --- Replay buffer ---
        #     "buffer_size": config.buffer_size,
        #
        #     # --- Optimization ---
        #     "lr": config.lr,
        #     "learning_starts": config.learning_starts,
        #     "train_batch_size": self.TRAIN_BATCH_SIZE,
        #     "batch_mode": "truncate_episodes",
        #
        #     # --- Parallelism ---
        #     "num_workers": 0,
        #     "num_gpus": 0,
        #     "num_gpus_per_worker": 0
        # }

        if config.debug:  # Run DQN within the same process (useful for debugging)
            dqn_trainer = dqn_config.build()
            for step in range(0, config.num_episodes * config.ep_len):
                dqn_trainer.train()
        else:
            tune.Tuner(
                "DQN",
                run_config=air.RunConfig(
                    name=exp_name,
                    verbose=2,
                    log_to_file=False,
                    local_dir=result_dir_path_ray,
                    stop={
                        "episodes_total": config.num_episodes
                    },
                    checkpoint_config=CheckpointConfig(
                        checkpoint_frequency=0,
                        checkpoint_at_end=False
                    )
                ),
                param_space=dqn_config.to_dict()
            ).fit()
            # trainer = DQNTrainer
            # run_experiments({
            #     exp_name: {
            #         "run": trainer,
            #         "env": "mdde_s",
            #         "stop": {
            #             "episodes_total": config.num_episodes,
            #         },
            #         "checkpoint_freq": 0,
            #         "local_dir": result_dir_path_ray,
            #         "restore": False,
            #         "config": exp_config
            #     },
            # }, verbose=0, reuse_actors=False)  # reuse_actors=True - messes up the results


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

    parser.add_argument('--debug',
                        help='Debug flag. If set, the agents are executed within the same process (without Tune).',
                        action='store_true')

    parser.add_argument('--sim',
                        help='Simulated benchmark (except the first run).',
                        action='store_true')

    # DQN params
    # https://docs.ray.io/en/master/rllib-algorithms.html#dqn
    # - Experiment length
    parser.add_argument('--num_episodes',
                        help='Total number of episodes.',
                        type=int,
                        default=1000)
    parser.add_argument('--ep_len',
                        help='Number of steps per episode.',
                        type=int,
                        default=1001)
    # - Replay buffer
    parser.add_argument('--buffer_size',
                        help='Size of the replay buffer.',
                        type=int,
                        default=int(1e6))

    # Optimization
    parser.add_argument('--lr',
                        help='Learning rate for the critic (Q-function) optimizer.',
                        type=float,
                        default=5e-4)
    parser.add_argument('--learning_starts',
                        help='How many steps of the model to sample before learning starts.',
                        type=int,
                        default=1000)

    # - MDDE scenario
    # -- Do-nothing action
    parser.add_argument('--do-nothing',
                        dest='do_nothing',
                        action='store_true',
                        help='(Default) Enable the "do nothing" action for agents.')
    parser.add_argument('--no-do-nothing',
                        dest='do_nothing',
                        action='store_false',
                        help='Disable the "do nothing" action for agents.')
    parser.set_defaults(do_nothing=True)

    parser.add_argument('--bench-psteps',
                        help='Frequency of benchmark execution (execute every N steps).',
                        type=int,
                        default=25)

    parser.add_argument('--bench-clients',
                        help='Number of benchmark clients.',
                        type=int,
                        default=50)

    parser.add_argument('--n-frags',
                        help='Number of fragments to generate.',
                        type=int,
                        default=20)

    parser.add_argument('--light',
                        help='Execute corresponding "light" workload.',
                        action='store_true')

    parser.add_argument('--ok-conf-a',
                        help='Ignore conflicting actions.',
                        action='store_true')

    config = parser.parse_args()

    DQNTestSample.run_result_dir = config.result_dir
    DQNTestSample.ray_temp_dir = config.temp_dir

    DQNTestSample.mdde_registry_host = config.reg_host
    DQNTestSample.mdde_registry_port = config.reg_port
    DQNTestSample.mdde_registry_config = config.config

    DQNTestSample.env_temp_dir = config.env_temp_dir

    runner = DQNTestSample()
    runner.setUp()
    runner.run_dqn(config)
