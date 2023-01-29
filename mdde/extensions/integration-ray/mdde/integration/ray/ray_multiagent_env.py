import logging
from typing import Union, Dict, Callable, Any

from gym.spaces import Discrete, Box
from ray import rllib
import numpy as np
from ray.rllib.utils.typing import MultiAgentDict, AgentID

from mdde.core import Environment


class MddeMultiAgentEnv(rllib.env.MultiAgentEnv):
    """
    MDDE wrapper for Ray RLlib's rllib.MultiAgentEnv.
    https://github.com/ray-project/ray/blob/master/rllib/env/multi_agent_env.py
    """

    def __init__(self,
                 env: Environment,
                 observation_shaper: Union[None, Callable[[np.ndarray], np.ndarray]] = None,
                 reward_scaler: Union[None, Callable[[float, Environment], float]] = None):
        """
        Initialize Ray environment.
        :param env: MDDE Environment.
        :type env: mdde.core.Environment
        :param observation_shaper: (optional) If specified, will be used for re-shaping the observations,
        otherwise the observations are flattened.
        :type observation_shaper: None or Callable[[np.ndarray], np.ndarray]
        :param reward_scaler: (optional) Reward scaler function.
        """
        logging.basicConfig(level=logging.INFO,
                            format="%(asctime)s [%(levelname)s] %(message)s")

        if not env:
            raise TypeError('env must be MDDE\'s Environment and can\'t be None')
        self._env = env
        """MDDE Environment instance."""

        self._observation_shaper = observation_shaper
        """Re-shaper of the environment observation."""

        self._reward_scaler = reward_scaler
        """Scale the reward."""

        init_obs_sample = MddeMultiAgentEnv.__get_mdde_observation_sample(observation_shaper=observation_shaper,
                                                                          mdde_env=env)
                
        self._agent_ids = init_obs_sample.keys()

        super().__init__()

    def reset(self) -> MultiAgentDict:
        """Resets the env and returns observations from ready agents.

        Returns:
            New observations for each ready agent.

        Examples:
            >>> from ray.rllib.env.multi_agent_env import MultiAgentEnv
            >>> class MyMultiAgentEnv(MultiAgentEnv): # doctest: +SKIP
            ...     # Define your env here. # doctest: +SKIP
            ...     ... # doctest: +SKIP
            >>> env = MyMultiAgentEnv() # doctest: +SKIP
            >>> obs = env.reset() # doctest: +SKIP
            >>> print(obs) # doctest: +SKIP
            {
                "car_0": [2.4, 1.6],
                "car_1": [3.4, -3.2],
                "traffic_light_1": [0, 3, 5, 1],
            }
        """
        obs, act_l = self._env.reset()
        obs_n: Dict[AgentID, Any] = {}
        for k, v in obs.items():
            obs_n[k] = self._shape_obs(observation_shaper=self._observation_shaper,
                                       agent_obs=v)
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
            obs_n[k] = self._shape_obs(observation_shaper=self._observation_shaper,
                                       agent_obs=v)
            # Done
            done_dict[k] = done[k]
            # Info
            info_dict[k] = {}  # TODO: return something meaningful here
        done_dict["__all__"] = all(d for d in done.values())

        # Re-scale the rewards if needed
        if self._reward_scaler is not None:
            scaled_rewards = {}
            for rew_k, rew_v in reward.items():
                scaled_rewards[rew_k] = self._reward_scaler(rew_v, self._env)
            reward = scaled_rewards

        return obs_n, reward, done_dict, info_dict

    @staticmethod
    def __get_mdde_observation_sample(observation_shaper, mdde_env):
        obs_n = {}
        observation, legal_actions = mdde_env.observation_space
        # MultiBinary(v) is not supported currently by Ray's MADDPG, making a Box instead.
        for k, v in observation.items():
            obs_n[k] = MddeMultiAgentEnv._box_obs(observation_shaper=observation_shaper, agent_obs=v)
        return obs_n

    @property
    def observation_space_dict(self) -> Dict[int, Box]:
        """
        Environment observation space shape.
        :return: Dictionary containing the shape of the observation space per agent.
        """
        return MddeMultiAgentEnv.__get_mdde_observation_sample(observation_shaper=self._observation_shaper,
                                                               mdde_env=self._env)

    @staticmethod
    def __get_mdde_action_space_sample(mdde_env):
        act_n: Dict[int, Discrete] = {}
        for k, v in mdde_env.action_space.items():
            act_n[k] = Discrete(v)
        return act_n

    @property
    def action_space_dict(self) -> Dict[int, Discrete]:
        """
        Environment action space shape
        :return: Dictionary containing the shape of the action space per agent
        """
        return MddeMultiAgentEnv.__get_mdde_action_space_sample(mdde_env=self._env)

    @staticmethod
    def _shape_obs(observation_shaper, agent_obs: np.ndarray) -> np.ndarray:
        """
        Reshape observations by either using the custom :func:`self.observation_shaper` or :func:`np.flatten()`.
        :param agent_obs: Observations as returned by the scenario.
        :type agent_obs: np.ndarray
        :return: Reshaped obsevations
        """
        if observation_shaper:
            v_float = observation_shaper(agent_obs)
        else:
            v_float = agent_obs.astype(np.float64).flatten()
        return v_float

    @staticmethod
    def _box_obs(observation_shaper, agent_obs: np.ndarray) -> Box:
        """
        Reshape observations and wrap into the Gym.Box shape.
        :param agent_obs: Observations as returned by the scenario.
        :type agent_obs: np.ndarray
        :return: 2D Box
        """
        v_float = MddeMultiAgentEnv._shape_obs(observation_shaper, agent_obs)
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
