import logging
from typing import Union, Dict, Callable

import gym.spaces as spaces  # Discrete, Box, Dict
import numpy as np

from mdde.core import Environment
from mdde.integration.ray import MddeMultiAgentEnv


class MddeMultiAgentEnvActionMask(MddeMultiAgentEnv):
    """
    MDDE wrapper for Ray RLlib's rllib.MultiAgentEnv supporting action masks.
    """

    def __init__(self, env: Environment, observation_shaper: Union[None, Callable[[np.ndarray], np.ndarray]] = None):
        """
        Initialize Ray environment.
        :param env: MDDE Environment.
        :type env: mdde.core.Environment
        :param observation_shaper: (optional) If specified, will be used for re-shaping the observations,
        otherwise the observations are flattened.
        :type observation_shaper: None or Callable[[np.ndarray], np.ndarray]
        """
        super().__init__(env, observation_shaper)

    def reset(self):
        obs, act_l = self._env.reset()
        obs_n = {}
        for k, v in obs.items():
            obs_n[k] = {'obs': self._shape_obs(v),
                        'action_mask': act_l[k]}
        return obs_n

    def step(self, action_dict):
        discrete_actions = {}
        for k, v in action_dict.items():
            if hasattr(type(v), '__iter__'):
                a_idx = np.argmax(v)  # Assuming the result is a flat array of probabilities
                discrete_actions[k] = a_idx
            else:
                discrete_actions[k] = v
        obs, reward, done, act_l = self._env.step(discrete_actions)
        obs_n = {}
        done_dict = {}
        info_dict = {}
        for k, v in obs.items():
            # Re-shape the observation
            obs_n[k] = {'obs': self._shape_obs(v),
                        'action_mask': act_l[k]}
            # Done
            done_dict[k] = done[k]
            # Info
            info_dict[k] = {}  # TODO: return something meaningful here
        done_dict["__all__"] = all(d for d in done.values())

        return obs_n, reward, done_dict, info_dict

    @property
    def observation_space_dict(self) -> Dict[int, Union[spaces.Box, spaces.Dict]]:
        """
        Environment observation space shape.
        :return: Dictionary containing the shape of the observation space per agent.
        """
        obs_n = {}
        # MultiBinary(v) is not supported currently by Ray's MADDPG, making a Box instead.
        observation, legal_actions = self._env.observation_space
        for k, v in observation.items():
            obs_n[k] = spaces.Dict({'obs': self._box_obs(v),
                                    'action_mask': spaces.Box(low=0.0, high=1.0,
                                                              shape=legal_actions[k].shape,
                                                              dtype=np.int8)})
        return obs_n

    @property
    def action_space_dict(self) -> Dict[int, spaces.Discrete]:
        """
        Environment action space shape
        :return: Dictionary containing the shape of the action space per agent
        """
        act_n: Dict[int, spaces.Discrete] = {}
        for k, v in self._env.action_space.items():
            act_n[k] = spaces.Discrete(v)
        return act_n
