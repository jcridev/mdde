from typing import Dict, Union

from gym.spaces import Discrete, Box
from ray import rllib
import numpy as np

from mdde.agent.default import DefaultAgent
from mdde.config import ConfigRegistry
from mdde.core import Environment
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.registry.tcp import RegistryClientTCP
from mdde.scenario.default import DefaultScenario


class MddeMultiAgentEnv(rllib.MultiAgentEnv):
    """
    https://github.com/ray-project/ray/blob/master/rllib/env/multi_agent_env.py
    """

    def __init__(self, **kvargs):
        self._env = self._make_env(**kvargs)

    def reset(self):
        """
        Resets the env and returns observations from ready agents.
        :return: obs (dict): New observations for each ready agent.
                Example:
                {
                    "car_0": [2.4, 1.6],
                    "car_1": [3.4, -3.2],
                    "traffic_light_1": [0, 3, 5, 1],
                }
        """
        obs = self._env.reset()
        obs_n = {}
        for k, v in obs.items():
            obs_n[k] = v.astype(np.float32).flatten()
        return obs_n

    def step(self, action_dict):
        """
        Returns observations from ready agents.

        :param action_dict: Dictionary of actions taken by every agent within a step.
                            Example:
                            action_dict={
                                "car_0": 1, "car_1": 0, "traffic_light_1": 2,
                            })

        :return: The returns are dicts mapping from agent_id strings to values.
                 The number of agents in the env can vary over time.

                 obs, rewards, dones, infos

                 obs (dict): New observations for each ready agent.
                 rewards (dict): Reward values for each ready agent.
                                 If the episode is just started, the value will be None.
                                 Example:
                                 {
                                    "car_0": 3,
                                    "car_1": -1,
                                    "traffic_light_1": 0,
                                 }

                 dones (dict): Done values for each ready agent.
                               The special key "__all__" (required) is used to indicate env termination.
                               Example:
                                {
                                    "car_0": False,    # car_0 is still running
                                    "car_1": True,     # car_1 is done
                                    "__all__": False,  # the env is not done
                                }
                 infos (dict): Optional info values for each agent id.
                               Example:
                                {
                                    "car_0": {},  # info for car_0
                                    "car_1": {},  # info for car_1
                                }
        """
        obs, reward = self._env.step(action_dict)
        obs_n = {}
        done_dict = {}
        info_dict = {}
        for k, v in obs.items():
            obs_n[k] = v.astype(np.float32).flatten()
            done_dict[k] = False  # TODO: real dictionary of the terminal states
            info_dict[k] = {"done": done_dict[k]}  # TODO: return something meaningful here
        done_dict["__all__"] = all(d for d in done_dict.values())

        return obs_n, reward, done_dict, info_dict

    @property
    def observation_space_dict(self) -> Dict[int, Union[Box]]:
        """
        Environment observation space shape
        :return: Dictionary containing the shape of the observation space per agent
        """
        obs_n = {}
        # MultiBinary(v) Currently not supported by Ray MADDPG, making a Box instead
        for k, v in self._env.observation_space.items():
            v_float = v.astype(np.float32).flatten()
            obs_n[k] = Box(low=0., high=1., shape=v_float.shape)
        return obs_n

    @property
    def action_space_dict(self) -> Dict[int, Discrete]:
        """
        Environment action space shape
        :return: Dictionary containing the shape of the action space per agent
        """
        act_n: Dict[int, Discrete] = {}
        for k, v in self._env.action_space.items():
            act_n[k] = Discrete(v)

        return act_n

    def _make_env(self, host: str, port: int, config: str) -> Environment:
        # TODO: Configurable scenario initialization

        # Create Registry client
        tcp_client = RegistryClientTCP(host, port)
        read_client: PRegistryReadClient = tcp_client
        write_client: PRegistryWriteClient = tcp_client
        ctrl_client: PRegistryControlClient = tcp_client

        # Create agents
        config_container = ConfigRegistry()
        config_container.read(config)

        agents = list()
        idx = 0
        for node in config_container.get_nodes():
            agents.append(DefaultAgent(node.id, idx, node.id))
            idx += 1

        # Create scenario
        scenario = DefaultScenario(100, agents)

        # Create environment
        environment = Environment(scenario, ctrl_client, write_client, read_client)
        # Re-generate data
        environment.initialize_registry()

        return environment
