import logging
from typing import Union, Callable

import gym
from gym.spaces import Discrete, Box, MultiBinary, MultiDiscrete
import numpy as np

from mdde.core import Environment


class MddeSignleAgentEnv(gym.Env):
    """
    https://docs.ray.io/en/master/rllib-env.html
    """

    def __init__(self,
                 env: Environment,
                 observation_shaper: Union[None, Callable[[np.ndarray], np.ndarray]] = None):
        """
        Initialize Ray environment.
        :param env: MDDE Environment.
        :type env: mdde.core.Environment
        :param observation_shaper: (optional) If specified, will be used for re-shaping the observations,
        otherwise the observations are flattened.
        :type observation_shaper: None or Callable[[np.ndarray], np.ndarray]
        """
        logging.basicConfig(level=logging.INFO,
                            format="%(asctime)s [%(levelname)s] %(message)s")

        if not env:
            raise TypeError('env must be MDDE\'s Environment and can\'t be None')
        self._env = env
        """MDDE Environment instance."""

        if len(self._env.agents) > 1:
            raise ValueError("Environment supplied to a single-agent wrapper must contain exactly one agent")

        self._agent = self._env.agents[0]
        """Agent info"""

        self._observation_shaper = observation_shaper
        """Re-shaper of the environment."""

    def reset(self):
        obs, act_l = self._env.reset()
        return self._shape_obs(obs[self._agent.id])

    def step(self, action):
        discrete_actions = {}
        a_idx = action  # np.argmax(action)  # Assuming the result is a flat array of probabilities
        discrete_actions[self._agent.id] = a_idx
        obs, reward, done, act_l = self._env.step(discrete_actions)

        obs_n = self._shape_obs(obs[self._agent.id])
        done_n = done[self._agent.id]
        info_n = {}
        reward_n = reward[self._agent.id]
        return obs_n, reward_n, done_n, info_n

    @property
    def observation_space(self) -> Union[Discrete, Box, MultiBinary, MultiDiscrete]:
        """
        Environment observation space shape.
        :return: Dictionary containing the shape of the observation space per agent.
        """
        observation, legal_actions = self._env.observation_space
        return self._box_obs(observation[self._agent.id])

    @property
    def action_space(self) -> Union[Discrete, Box, MultiBinary, MultiDiscrete]:
        """
        Environment action space shape
        :return: Dictionary containing the shape of the action space per agent
        """
        return Discrete(self._env.action_space[self._agent.id])

    def _shape_obs(self, agent_obs: np.ndarray) -> np.ndarray:
        """
        Reshape observations by either using the custom :func:`self.observation_shaper` or :func:`np.flatten()`.
        :param agent_obs: Observations as returned by the scenario.
        :type agent_obs: np.ndarray
        :return: Reshaped obsevations
        """
        if self._observation_shaper:
            v_float = self._observation_shaper(agent_obs)
        else:
            v_float = agent_obs.astype(np.float64).flatten()
        return v_float

    def _box_obs(self, agent_obs: np.ndarray) -> Box:
        """
        Reshape observations and wrap into the Gym.Box shape.
        :param agent_obs: Observations as returned by the scenario.
        :type agent_obs: np.ndarray
        :return: 2D Box
        """
        v_float = self._shape_obs(agent_obs)
        return Box(low=0.0, high=1.0, shape=v_float.shape, dtype=np.float64)

    @staticmethod
    def configure_ray(ray) -> None:
        """
        Any additional configuration of Ray. Call before creating the environment
        :param ray: Ray instance
        """
        from mdde.config import ConfigEnvironment, ConfigEnvironmentYaml
        ray.register_custom_serializer(
            ConfigEnvironment,
            serializer=ConfigEnvironmentYaml.ConfigEnvironmentYamlSerializer.serialize,
            deserializer=ConfigEnvironmentYaml.ConfigEnvironmentYamlSerializer.deserialize)

    def render(self, mode='human'):
        pass