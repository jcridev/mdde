from ray import rllib


# TODO: Make documentation examples MDDE specific (now these are copies from the Ray documentation)
class MddeMultiAgentEnv(rllib.MultiAgentEnv):
    """
    https://github.com/ray-project/ray/blob/master/rllib/env/multi_agent_env.py
    """

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
        pass

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
        pass
