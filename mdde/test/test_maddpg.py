import unittest
import os

import ray
from ray import utils
from ray.tune import run_experiments
from ray.tune.registry import register_trainable, register_env
from ray.rllib.contrib.maddpg.maddpg import MADDPGTrainer

from mdde.core import Environment
from mdde.integration.ray.ray_multiagent_env import MddeMultiAgentEnv


# https://ray.readthedocs.io/en/latest/installation.html


class MaddpgTestCases(unittest.TestCase):  #
    REGISTRY_HOST = 'localhost'
    REGISTRY_PORT = 8942
    TEST_CONFIG_FILE = '../../test/registry_config.yml'

    TEST_RESULT_DIR = '../../test/debug/result'
    """
    For tests, make sure "TEST_TEMP_DIR" not too long for the plasma store, otherwise ray will fail
    """
    TEST_TEMP_DIR = '/mnt/hdd-a500/Temp'  #'../../test/debug/temp'

    NUM_EPISODES = 1000
    EPISODE_LEN = 25
    LEARNING_RATE = 1e-2
    NUM_ADVERSARIES = 0
    SAMPLE_BATCH_SIZE = 25
    TRAIN_BATCH_SIZE = 100
    ADV_POLICY = 'maddpg'
    GOOD_POLICY = 'maddpg'

    def setUp(self) -> None:
        os.environ['TF_CPP_MIN_LOG_LEVEL'] = '1'  # {'0': 'DEBUG', '1': 'INFO', '2': 'WARNING', '3': 'ERROR'}

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

    def test_maddpg(self):
        temp_dir_full_path = os.path.realpath(self.TEST_TEMP_DIR)
        result_dur_full_path = os.path.realpath(self.TEST_RESULT_DIR)
        config_file_full_path = os.path.realpath(self.TEST_CONFIG_FILE)

        ray.init(redis_max_memory=int(ray.utils.get_system_memory() * 0.4),
                 memory=int(ray.utils.get_system_memory() * 0.2),
                 object_store_memory=int(ray.utils.get_system_memory() * 0.2),
                 num_gpus=0,
                 num_cpus=4,
                 temp_dir=temp_dir_full_path)

        MADDPGAgent = MADDPGTrainer.with_updates(
            mixins=[MaddpgTestCases.CustomStdOut]
        )
        register_trainable("MADDPG", MADDPGAgent)

        # Create and initialize environment before passing it to Ray
        # This makes it impossible to run multiple instances of the environment, however it's intentional due to the
        # the nature of the environment that's represented as a distributed infrastructure of services, it can't be
        # easily created and destroyed as a simple local game-like environment
        env_instance = MddeMultiAgentEnv(host=self.REGISTRY_HOST,
                                         port=self.REGISTRY_PORT,
                                         config=config_file_full_path)

        def env_creator(kvargs):
            return MddeMultiAgentEnv(**kvargs)

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
                    "use_local_critic": use_local_critic[i],
                    "obs_space_dict": env_instance.observation_space_dict,
                    "act_space_dict": env_instance.action_space_dict,
                }
            )

        policies = {"policy_%d" % i: gen_policy(i) for i in env_instance.action_space_dict.keys()}
        policy_ids = list(policies.keys())

        def policy_mapping_fn(agent_id):
            return policy_ids[agent_id]

        exp_name = "MADDPG_MDDE_DEBUG"

        run_experiments({
            exp_name: {
                "run": "contrib/MADDPG",
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
                        "host": self.REGISTRY_HOST,
                        "port": self.REGISTRY_PORT,
                        "config": config_file_full_path
                    },
                    "num_envs_per_worker": 1,
                    "horizon": self.EPISODE_LEN,

                    # === Policy Config ===
                    # --- Model ---
                    "good_policy": self.GOOD_POLICY,
                    "adv_policy": self.ADV_POLICY,
                    "actor_hiddens": [64] * 2,
                    "actor_hidden_activation": "relu",
                    "critic_hiddens": [64] * 2,
                    "critic_hidden_activation": "relu",
                    "n_step": 1,
                    "gamma": 0.95,

                    # --- Exploration ---
                    "tau": 0.01,

                    # --- Replay buffer ---
                    "buffer_size": 10000,

                    # --- Optimization ---
                    "actor_lr": self.LEARNING_RATE,
                    "critic_lr": self.LEARNING_RATE,
                    "learning_starts": self.TRAIN_BATCH_SIZE * self.EPISODE_LEN,
                    "sample_batch_size": self.SAMPLE_BATCH_SIZE,
                    "train_batch_size": self.TRAIN_BATCH_SIZE,
                    "batch_mode": "truncate_episodes",

                    # --- Parallelism ---
                    "num_workers": 1,
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
    runner = MaddpgTestCases()
    runner.setUp()
    runner.test_maddpg()
    #unittest.main()
