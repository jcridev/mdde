from typing import Dict

from gym.spaces import Discrete, Box, MultiBinary
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

    # TODO: Make documentation examples MDDE specific (now these are copies from the Ray documentation)
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
        self._env.reset()

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
        return self._env.step(action_dict)

    @property
    def observation_space_dict(self) -> Dict[int, MultiBinary]:
        obs_n = {}
        for k, v in self._env.observation_space.items():
            obs_n[k] = MultiBinary(v)  # Currently not supported by Ray MADDPG
            #obs_n[k] = Box(low=np.zeros((space.n,)), high=np.ones((space.n,)))
        return obs_n

    @property
    def action_space_dict(self) -> Dict[int, Discrete]:
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
