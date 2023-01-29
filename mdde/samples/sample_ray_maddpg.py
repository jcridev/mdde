import os
import argparse
import logging

from pathlib import Path
from typing import Callable, Union

import ray
from ray import air
from ray import tune
from ray.air import CheckpointConfig
from ray.tune.registry import register_env
from ray.rllib.algorithms.maddpg import maddpg

from mdde.core import Environment
from mdde.agent.default import SingleNodeDefaultAgent
from mdde.registry.workload import EDefaultYCSBWorkload
from mdde.scenario.default import DefaultScenario, DefaultScenarioSimulation
from mdde.config import ConfigRegistry, ConfigEnvironment
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.registry.tcp import RegistryClientTCP
from mdde.integration.ray.ray_multiagent_env import MddeMultiAgentEnv


# https://ray.readthedocs.io/en/latest/installation.html


class MADDPGSample:
    """Demonstration sample code showing how RAY's MADDPG can be executed used with MDDE."""

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

    NUM_ADVERSARIES = 0
    ADV_POLICY = 'maddpg'
    GOOD_POLICY = 'maddpg'

    def setUp(self) -> None:
        os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'  # {'0': 'DEBUG', '1': 'INFO', '2': 'WARNING', '3': 'ERROR'}

    class CustomStdOut(object):
        def _log_result(self, result):
            if result["training_iteration"] % 50 == 0:
                try:
                    print("steps: {}, episodes: {}, mean episode reward: {}, agent episode reward: {}, time: {}".format(
                        result["timesteps_total"],
                        result["episodes_total"],
                        result["episode_reward_mean"],
                        result["policy_reward_mean"],
                        round(result["time_total_s"] - self.cur_time, 3)
                    ))
                except:
                    pass

                self.cur_time = result["time_total_s"]

    def run_maddpg(self, config):
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

        ray.init(num_gpus=0,
                 num_cpus=4,
                 # temp_dir=temp_dir_full_path
                 )

        # MddeMultiAgentEnv.configure_ray(ray)

        # maddpg_agent = MADDPG.with_updates(
        #     mixins=[MADDPGSample.CustomStdOut]
        # )
        # register_trainable("MADDPG", maddpg_agent)

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
                                                     allow_do_nothing=do_nothing))
                idx += 1

            # Create scenario
            num_fragments: int = config.n_frags
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

            # Set multiplier to the sore related term of the default reward function
            scenario.set_storage_importance(config.store_m)
            # Set ho much do-nothing worth
            scenario.set_do_nothing_worth(config.dn_worth)

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

        sample_selected_shaper = None
        """Observation shaper selected. Set None if you want to use the default one in the wrapper."""
        if config.o_shape == 'flatbox':
            sample_selected_shaper = obs_shaper_flat_box
        elif config.o_shape == '2dbox':
            sample_selected_shaper = obs_shaper_2d_box

        if sample_selected_shaper is None:
            raise RuntimeError("Unknown observation shaper")

        # Configure reward scaler
        reward_scaler: Union[Callable[[float, Environment], float], None] = None

        if config.scale_rew:
            def scale_reward(reward: float, env: Environment):
                """A very simplistic reward scaling function, oriented for use with the counterfeit benchmark
                and default scenario"""
                if reward < 0:
                    return reward
                counterfeit_thr_max = env.counterfeit_throughput_max
                counterfeit_thr_min = env.counterfeit_throughput_min if config.bench_psteps == 1 else 0.0
                scenario_rew_max = 1.0
                scenario_rew_min = 0.0
                default_scenario_thr_max = counterfeit_thr_max + counterfeit_thr_max * config.store_m
                scaled_reward = (
                                        ((reward - counterfeit_thr_min) / (
                                                    default_scenario_thr_max - counterfeit_thr_min))
                                        * (scenario_rew_max - scenario_rew_min)
                                # Unnecessary here, but easier for debug
                                ) + scenario_rew_min
                return scaled_reward

            reward_scaler = scale_reward

        # Create and initialize environment before passing it to Ray
        # This makes it impossible to run multiple instances of the environment, however it's intentional due to the
        # the nature of the environment that's represented as a distributed infrastructure of services, it can't be
        # easily created and destroyed as a simple local game-like environment
        env_instance = MddeMultiAgentEnv(env=make_env(host=self.mdde_registry_host,
                                                      port=self.mdde_registry_port,
                                                      reg_config=config_file_full_path,
                                                      env_config=mdde_config,
                                                      write_stats=False,
                                                      initial_benchmark=False,
                                                      do_nothing=config.do_nothing),
                                         observation_shaper=sample_selected_shaper,
                                         reward_scaler=reward_scaler)

        input_size = env_instance.observation_space_dict[0].shape[0]
        output_size = env_instance.action_space_dict[0].n

        if config.hidden_dim is not None:
            maddpg_network = [config.hidden_dim] * 2
        else:
            maddpg_network = [int((input_size + output_size) * 0.8)] * 2  # [64] * 2

        def env_creator(kvargs):
            env = make_env(**kvargs)
            return MddeMultiAgentEnv(env=env,
                                     observation_shaper=sample_selected_shaper,
                                     reward_scaler=reward_scaler)

        register_env("mdde", env_creator)

        # generate policies based on the created environment instance
        def gen_policy(i):
            use_local_critic = [
                self.ADV_POLICY == "ddpg" if i < 0 else
                self.GOOD_POLICY == "ddpg" for i in range(4)
            ]
            return (
                None,
                env_instance.observation_space_dict[i],
                env_instance.action_space_dict[i],
                {
                    "agent_id": i,
                    "use_state_preprocessor": sample_selected_shaper == obs_shaper_2d_box,
                    "use_local_critic": use_local_critic[i],
                    "obs_space_dict": env_instance.observation_space_dict,
                    "act_space_dict": env_instance.action_space_dict,
                }
            )

        policies = {"policy_%d" % i: gen_policy(i) for i in env_instance.action_space_dict.keys()}
        policy_ids = list(policies.keys())

        def policy_mapping_fn(agent_id):
            return policy_ids[agent_id]

        maddpg_config = (
            maddpg.MADDPGConfig()
            .environment(
                env='mdde',
                env_config={
                    "host": self.mdde_registry_host,
                    "port": self.mdde_registry_port,
                    "reg_config": config_file_full_path,
                    "env_config": mdde_config,
                    "write_stats": True,
                    "do_nothing": config.do_nothing
                },
            )
            .multi_agent(
                policies=policies,
                policy_mapping_fn=policy_mapping_fn,
            )
        )

        exp_name = "MADDPG_MDDE_DEBUG"

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
        #     "good_policy": self.GOOD_POLICY,
        #     "adv_policy": self.ADV_POLICY,
        #     "actor_hiddens": maddpg_network,
        #     "actor_hidden_activation": "relu",
        #     "critic_hiddens": maddpg_network,
        #     "critic_hidden_activation": "relu",
        #     "n_step": 1,
        #     "gamma": config.gamma,
        #
        #     # --- Exploration ---
        #     "tau": config.tau,
        #
        #     # --- Replay buffer ---
        #     "buffer_size": config.buffer_size,
        #
        #     # --- Optimization ---
        #     "actor_lr": config.actor_lr,
        #     "critic_lr": config.critic_lr,
        #     "learning_starts": config.learning_starts,
        #     # "sample_batch_size": config.smpl_batch_s,
        #     "train_batch_size": config.trn_batch_s,
        #     # "batch_mode": "truncate_episodes",
        #
        #     # --- Parallelism ---
        #     "num_workers": 0,  # run only one env process
        #     "num_gpus": 0,
        #     "num_gpus_per_worker": 0,
        #
        #     # === Multi-agent setting ===
        #     "multiagent": {
        #         "policies": policies,
        #         "policy_mapping_fn": policy_mapping_fn
        #     },
        # }

        if config.debug:  # Run MADDPG within the same process (useful for debugging)
            maddpg_trainer = maddpg_config.build()
            for step in range(0, config.num_episodes * config.ep_len):
                maddpg_trainer.train()
        else:  # Run using Tune
            tune.Tuner(
                "MADDPG",
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
                param_space=maddpg_config.to_dict()
            ).fit()

            # run_experiments({
            #     exp_name: {
            #         "run": "contrib/MADDPG",
            #         "env": "mdde",
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

    # MADDPG params
    # Descriptions source: https://docs.ray.io/en/master/rllib-algorithms.html#maddpg
    # Note: We omit parameters that make no sense as experimental variables in this sample.
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

    # - Optimization
    parser.add_argument('--critic_lr',
                        help='Learning rate for the critic (Q-function) optimizer.',
                        type=float,
                        default=1e-2)
    parser.add_argument('--actor_lr',
                        help='Learning rate for the actor (policy) optimizer.',
                        type=float,
                        default=1e-2)
    parser.add_argument('--tau',
                        help='Update the target by tau * policy + (1-tau) * target_policy.',
                        type=float,
                        default=0.01)
    parser.add_argument('--actor_feature_reg',
                        help='Weights for feature regularization for the actor.',
                        type=float,
                        default=0.001)
    parser.add_argument('--grad_norm_clipping',
                        help='If not None, clip gradients during optimization at this value.',
                        type=float,
                        default=0.5)
    parser.add_argument('--learning_starts',
                        help='How many steps of the model to sample before learning starts.',
                        type=int,
                        default=1001 * 25)

    parser.add_argument('--smpl-batch-s',
                        help='Sample batch size.',
                        type=int,
                        default=25)

    parser.add_argument('--trn-batch-s',
                        help='Train batch size.',
                        type=int,
                        default=100)
    # - Q-learning
    parser.add_argument('--gamma',
                        help='Discount factor (Q-learning) ∈ [0, 1]. If closer to zero, the agent will give more '
                             'weight to the most recent rewards. While, closer to 1 will take into consideration '
                             'future rewards making agent striving to higher rewards in the future than being content'
                             'with the current reward.',
                        type=float,
                        default=0.95)

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

    parser.add_argument('--store-m',
                        help='Importance multiplier for the storage term of the default reward function.'
                             '0.0 - ignore (agents are allowed to hoard everything with no repercussions)',
                        type=float,
                        default=0.5)

    parser.add_argument('--dn-worth',
                        help='How much reward should be given to the agent that decide to do nothing in the iteration.',
                        type=float,
                        default=1.0)

    parser.add_argument('--n-frags',
                        help='Number of fragments to generate.',
                        type=int,
                        default=20)

    parser.add_argument('--bench-clients',
                        help='Number of benchmark clients.',
                        type=int,
                        default=50)

    parser.add_argument('--hidden-dim',
                        help='Number if neurons in the network layers.',
                        type=int,
                        default=None)

    parser.add_argument('--light',
                        help='Execute corresponding "light" workload.',
                        action='store_true')

    parser.add_argument('--o-shape',
                        help='Select the observation space shaper. "flatbox" or "2dbox"',
                        type=str,
                        default='flatbox')

    parser.add_argument('--ok-conf-a',
                        help='Ignore conflicting actions.',
                        action='store_true')

    parser.add_argument('--scale-rew',
                        help='Scale reward to the range: [-1.0, 1.0].',
                        action='store_true')

    config = parser.parse_args()

    MADDPGSample.run_result_dir = config.result_dir
    MADDPGSample.ray_temp_dir = config.temp_dir

    MADDPGSample.mdde_registry_host = config.reg_host
    MADDPGSample.mdde_registry_port = config.reg_port
    MADDPGSample.mdde_registry_config = config.config

    MADDPGSample.env_temp_dir = config.env_temp_dir

    runner = MADDPGSample()
    runner.setUp()
    runner.run_maddpg(config)
