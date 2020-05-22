import logging
import time
import csv
import sqlite3
import pickle
import zlib
from pathlib import Path
from typing import Set, Tuple, Dict, Union, AnyStr

import numpy as np

from mdde.agent.abc import ABCAgent
from mdde.config import ConfigEnvironment
from mdde.core.exception import EnvironmentInitializationError
from mdde.registry.container import BenchmarkStatus
from mdde.registry.protocol import PRegistryControlClient, PRegistryWriteClient, PRegistryReadClient
from mdde.scenario.abc import ABCScenario
from mdde.registry.enums import ERegistryMode
from mdde.util import assert_with_log


class Environment:
    """Entry point to MDDE. Reinforcement learning frameworks should be wrapped around this class to function"""

    def __init__(self,
                 config: Union[None, ConfigEnvironment],
                 scenario: ABCScenario,
                 registry_ctrl: PRegistryControlClient,
                 registry_write: PRegistryWriteClient,
                 registry_read: PRegistryReadClient,
                 experiment_id: Union[None, str] = None,
                 write_stats: bool = False):
        """
        Environment constructor
        :param config: (optional) MDDE configuration object
        :param scenario: Scenario object implementing ABCScenario
        :param registry_ctrl: Control commands for the MDDE registry implementation
        :param registry_write: Write commands for the MDDE registry implementation
        :param registry_read: Read commands for the MDDE registry implementation
        :param experiment_id: (optional) Id of the current experiment. Used for identification of the environment
        instance. If not explicitly specified a random id is generated. Id is an alphanumeric string with the max length
        of 8. Symbols beyond 8 will be ignored, special characters and spaces will be removed.
        """
        if not isinstance(scenario, ABCScenario):
            raise TypeError("scenario must extend ABCScenario")

        if registry_ctrl is None:
            raise TypeError("registry control client can't be None")
        if registry_write is None:
            raise TypeError("registry write client can't be None")
        if registry_read is None:
            raise TypeError("registry read client can't be None")

        if scenario is None:
            raise TypeError("scenario can't be None")

        self._experiment_id = self.__generate_exp_id(experiment_id)
        # Attach scenario and agents to this experiment
        scenario.attach_to_experiment(self.experiment_id)

        self._logger = logging.getLogger('Env_{}'.format(self.experiment_id))
        """Environment instance specific logger."""

        self._scenario: ABCScenario = scenario
        """Scenario executed within the current environment."""
        self._registry_ctrl: PRegistryControlClient = registry_ctrl
        """Control commands for the MDDE registry implementation."""
        self._registry_write: PRegistryWriteClient = registry_write
        """Write commands for the MDDE registry implementation."""
        self._registry_read: PRegistryReadClient = registry_read
        """Read commands for the MDDE registry implementation."""

        self._config: Union[None, ConfigEnvironment] = config
        """Environment config file, if required. Injected into the scenario."""

        # Internal properties used for statistics
        self.__step_count: int = 0
        """Total number steps taken within this experiment."""
        self.__episode_count: int = 0
        """Total number of episodes (Resets)."""
        self.__did_reset_flag: bool = False
        """Flag set to True after a reset is performed, then set to False after a step taken."""

        self._write_stats = write_stats
        """True - write additional statistics to the results folder."""
        self.__mdde_result_folder_root: Union[None, str] = None
        if self._config:
            self.__mdde_result_folder_root = self._config.result_dir(self, get_root=True)
            """Folder where all of the statistical data should be dumped."""

        if self._write_stats and not self.__mdde_result_folder_root:
            raise AssertionError("Stats can't be writen, results folder is not configured.")

        self.__file_csv_writer = None

        self.activate_scenario()

    def __del__(self):
        self._logger.info('Shutting down')
        if self.__file_csv_writer:
            # Close a CSV writer if there's one
            self._logger.info('Closing CSV writer')
            file = self.__file_csv_writer[0]
            file.close()

    @staticmethod
    def __generate_exp_id(id_prototype: Union[None, str]) -> str:
        """
        Generates a new random Experiment ID or processes a passed value.
        :param id_prototype: (optional) Prototype string. Will be cleansed out of all non-alphanumeric characters and
        shortened to the maximum of 8 characters.
        :return: An alphanumeric string of max 8 characters.
        """
        if id_prototype is None:
            import uuid
            gen_id = uuid.uuid4()
        else:
            import re
            gen_id = re.sub('[^A-Za-z0-9]+', '', id_prototype)
            if len(gen_id) == 0:
                raise ValueError("Environment ID must consist out of alphanumeric values")
        return str(gen_id)[:8]

    @property
    def experiment_id(self) -> str:
        """
        Experiment id.
        :return: An alphanumeric string of max 8 characters.
        """
        return self._experiment_id

    def activate_scenario(self) -> None:
        """
        Verify the current scenario and ensure it's basic correctness before the start of experiments
        """
        self._scenario.inject_config(self._config)
        # Make sure the same data node isn't assigned to more than one agent at a time
        if len(self._scenario.get_agents()) > 1:
            nodes_per_agent: [Set[str]] = []
            for agent in self._scenario.get_agents():
                nodes_per_agent.append(set(agent.data_node_ids))
            if len(set.intersection(*nodes_per_agent)) > 0:
                raise ValueError("The same data node id can't be assigned to more than one agent at the time")
        # Attach registry clients to the agents
        for agent in self._scenario.get_agents():
            agent.attach_registry(self._registry_read, self._registry_write)

    def initialize_registry(self, with_benchmark: bool = True) -> None:
        """
        Initialize or re-initialize the registry. All existing data will be removed, all data generated anew.
        :param with_benchmark:
        :return:
        """
        self._logger.info("Environment initialization starting")
        # Registry must be in the 'shuffle' mode
        self._set_registry_mode(ERegistryMode.shuffle)
        # Flush existing data
        self._logger.info("Flushing current environment data")
        flush_result = self._registry_ctrl.ctrl_flush()
        if flush_result.failed:
            raise EnvironmentInitializationError(flush_result.error)
        # Re-initialize nodes
        self._logger.info("Initializing nodes from the registry config")
        nodes_populate_res = self._registry_ctrl.ctrl_populate_default_nodes()
        if nodes_populate_res.failed:
            raise EnvironmentInitializationError(nodes_populate_res.error)
        # Registry must be in the 'benchmark' mode, meaning not accepting any modification (write) commands
        self._set_registry_mode(ERegistryMode.benchmark)
        # Generate data
        self._logger.info("Executing workload to generate data")
        data_gen_result = self._registry_ctrl.ctrl_generate_data(self._scenario.get_data_generator_workload())
        if data_gen_result.failed:
            err = EnvironmentInitializationError(data_gen_result.error)
            self._logger.critical(err)
            raise err
        if not data_gen_result.result:
            err = EnvironmentInitializationError("Initial data was not generated, "
                                                 "check the registry logs for more information")
            self._logger.critical(err)
            raise err
        # Run initial fragmentation
        self._logger.info("Fragment generation starting")
        fragmenter = self._scenario.get_fragmenter()
        requires_shuffle, fragments_ids = fragmenter.run_fragmentation(self._registry_read, self._registry_write)
        self._scenario.inject_fragments(fragments_ids)
        self._logger.info("Finished fragmentation, shuffle required: %r", requires_shuffle)
        # Switch to shuffle
        self._set_registry_mode(ERegistryMode.shuffle)
        # Shuffle tuples if fragmentation introduced any changes in the registry
        if requires_shuffle:
            registry_to_data_sync_result = self._registry_ctrl.ctrl_sync_registry_to_data()
            if registry_to_data_sync_result.failed:
                err = EnvironmentInitializationError(data_gen_result.error)
                self._logger.critical(err)
                raise err
        # Create an initial default snapshot (Environment will roll back to this snapshot at reset)
        self._logger.info("Creating the initial default snapshot")
        snapshot_create_result = self._registry_ctrl.ctrl_snapshot_create(True)
        if snapshot_create_result.failed:
            err = EnvironmentInitializationError(snapshot_create_result.error)
            self._logger.critical(err)
            raise err
        default_snapshot_id = snapshot_create_result.result
        if default_snapshot_id:
            self._logger.info("Default snapshot created with ID: %s", default_snapshot_id)
        else:
            err = EnvironmentInitializationError("Failed to create a new default snapshot, "
                                                 "no ID returned from the registry")
            self._logger.critical(err)
            raise err
        # Initialize the action space
        self._logger.info("Initializing action space per agent")
        self._initialize_action_space()
        # Run the initial benchmark to establish a baseline
        if with_benchmark:
            self.benchmark()
        self._logger.info("Environment initialization is complete")

    def reset(self) -> Tuple[Dict[int, np.ndarray], Dict[int, np.ndarray]]:
        """
        Reset the environment, reverting to the original data snapshot.
        :return: Observation space state after the reset per agent; Legal actions per agent in the current state;
        """
        self._logger.info("Resetting the environment")
        # Writes stats if needed
        if self._write_stats:
            self._logger.info("Saving the current observations of the environment")
            obs_n, legal_act = self.observation_space
            self._dump_observation_to_sqlite(self.__step_count, self.__episode_count, obs_n, reset=True)
        # Call registry reset
        self._set_registry_mode(ERegistryMode.benchmark)
        reset_call_response = self._registry_ctrl.ctrl_reset()
        if reset_call_response.failed:
            raise RuntimeError(reset_call_response.error)
        # Reset agents state
        self._scenario.reset()
        self.__next_episode()
        # Execute the initial benchmark
        self.benchmark()
        # Retrieve the observations
        return self.observation_space

    def __next_episode(self):
        self.__did_reset_flag = True
        self.__episode_count += 1

    def step(self, action_n: Dict[int, int]) \
            -> Tuple[Dict[int, np.ndarray], Dict[int, float], Dict[int, bool], Dict[int, np.ndarray]]:
        """
        Execute actions chosen for each agent, get resulted rewards and new observations
        :param action_n: Dict['agent_id':action_id]
        :return: Dict['agent_id':'np.ndarray of observations'], Dict['agent_id':'reward'], Dict['agent_id':'done flag'],
        Dict['agent_id':'np.ndarray of legality or illegality of actions in the current state']
        """
        self.__step_count += 1
        # Act
        self._scenario.make_collective_step(action_n)
        # Measure
        bench_execute_step = self._scenario.do_run_benchmark()
        if bench_execute_step:
            # Run the benchmark now if appropriate for the current scenario
            self.benchmark()
        # Observe
        obs_n, legal_act_n = self.observation_space
        # Get the reward
        reward_n = self._scenario.get_reward()
        # Get the done flag
        done_n = {a.id: a.done for a in self._scenario.get_agents()}

        assert_with_log(obs_n is not None, "Unable to retrieve observations", self._logger)
        assert_with_log(reward_n is not None, "Unable to retrieve rewards", self._logger)
        # Writes stats if needed
        if self._write_stats:
            self._dump_action_reward_to_csv(step_idx=self.__step_count,
                                            episode_idx=self.__episode_count,
                                            action_n=action_n,
                                            reward_n=reward_n,
                                            done_n=done_n,
                                            after_reset=self.__did_reset_flag)
            if bench_execute_step:
                # Dump observations after a benchmark run
                self._dump_observation_to_sqlite(self.__step_count, self.__episode_count, obs_n, reset=False)
        self.__did_reset_flag = False
        return obs_n, reward_n, done_n, legal_act_n

    def _dump_action_reward_to_csv(self,
                                   step_idx: int,
                                   episode_idx: int,
                                   action_n: Dict[int, int],
                                   reward_n: Dict[int, float],
                                   done_n: Dict[int, bool],
                                   after_reset: bool,
                                   timestamp: Union[None, int, float, AnyStr] = None) -> None:
        """
        Write statistics data to a CSV file.
        Opens a CSV writer that remains open for the duration of the run.
        :param step_idx: Current step.
        :param episode_idx: Current episode.
        :param action_n: {agent_id: action_id}.
        :param reward_n: {agent_id: reward}.
        :param done_n: {agent_id: done_flag}.
        :param after_reset: True - first step after reset.
        :param timestamp: (optional) Step timestamp. Default is Unix time in nanoseconds.
        """
        active_agents = self._scenario.get_agents()
        if not self.__file_csv_writer:
            file_path = Path(self.__mdde_result_folder_root).joinpath('action_rewards.csv')
            csv_file = open(file_path, 'w', newline='')
            csv_writer = csv.writer(csv_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
            self.__file_csv_writer = (csv_file, csv_writer)
            # Generate a header
            header = ['episode', 'step']
            for agent in active_agents:
                header.append("action_{}".format(agent.id))
                header.append("reward_{}".format(agent.id))
                header.append("done_{}".format(agent.id))
            header.append('reset')
            header.append('ts')
            csv_writer.writerow(header)
        csv_writer = self.__file_csv_writer[1]
        row = [episode_idx, step_idx]
        for agent in active_agents:
            row.extend([action_n.get(agent.id),
                        reward_n.get(agent.id),
                        1 if done_n.get(agent.id) else 0])
        row.append(1 if after_reset else 0)  # First step after reset flag
        if not timestamp:
            row.append(time.time_ns())  # Step timestamp in nanoseconds
        else:
            row.append(timestamp)
        csv_writer.writerow(row)

    def _dump_observation_to_sqlite(self,
                                    step_idx: int,
                                    episode_idx: int,
                                    obs_n: Dict[int, np.ndarray],
                                    reset: bool
                                    ) -> None:
        """
        Save observations for agents to an SQLite file.
        :param step_idx: Current step.
        :param episode_idx: Current episode.
        :param obs_n: Dictionary of observations per agents for current step.
        :param reset: True if it's an observation made right before the reset.
        """
        # TODO: A more efficient way to store a large number of matrices for later analysis
        db_path = Path(self.__mdde_result_folder_root).joinpath("obs.db")
        db_exists = db_path.is_file()
        with sqlite3.connect(db_path) as conn:
            cur = conn.cursor()
            if not db_exists:  # Create schema if a newly created file
                schema_q = 'CREATE TABLE IF NOT EXISTS observations (' \
                           'episode INTEGER NOT NULL, step INTEGER NOT NULL, agent INTEGER NOT NULL, ' \
                           'reset BOOLEAN NOT NULL, shape BLOB NOT NULL, obs BLOB NOT NULL, ' \
                           'PRIMARY KEY (episode, step, agent, reset));'
                cur.execute(schema_q)
            insert_q = "INSERT INTO observations (episode, step, agent, reset, shape, obs) VALUES (?, ?, ?, ?, ?, ?)"
            for agent_id, obs in obs_n.items():
                cur.execute(insert_q, [episode_idx, step_idx, agent_id,
                                       1 if reset else 0,
                                       pickle.dumps(obs.shape, 0),  # pickle the shape of the observation
                                       zlib.compress(obs.tobytes(order='C'))])  # dump np.ndarray to bytes
                # To restore the observation:
                #   1. Unpickle the shape
                #   2. a = numpy.frombuffer(zlib.decompress(obs))
                #   3. reshape a to the original shape

    @property
    def agents(self) -> Union[None, Tuple[ABCAgent]]:
        """
        If the scenario is set, return the agents defined in the active scenario
        :return: Tuple of agents
        """
        if self._scenario is None:
            return None
        return self._scenario.get_agents()

    @property
    def observation_space(self) -> Tuple[Dict[int, np.ndarray], Dict[int, np.ndarray]]:
        """
        Retrieve all observation spaces for all agents.
        :return: Two dictionaries where keys are agent ids: [0] dictionary of observations per agent;
        [1] dictionary of the indexes of valid actions per agent in the current state of the environment.
        """
        return self._scenario.get_observation(registry_read=self._registry_read)

    @property
    def action_space(self) -> Dict[int, int]:
        """
        Retrieve the action space per agent.
        :return: Dict['agent_id', number_of_discrete_actions]
        """
        act_n: Dict[int, int] = {}
        for agent in self._scenario.get_agents():
            act_n[agent.id] = agent.get_actions()
        return act_n

    def benchmark(self, without_waiting: bool = False) -> Union[None, BenchmarkStatus]:
        """Execute benchmark run"""
        # Execute the shuffle queue
        self._set_registry_mode(target_mode=ERegistryMode.shuffle)
        self._logger.info('Executing the shuffle queue')
        sync_data_result = self._registry_ctrl.ctrl_sync_registry_to_data()
        if sync_data_result.failed:
            raise RuntimeError(sync_data_result.error)
        s_queue_res_msg = "Shuffle queue executed: {}".format(sync_data_result.result)
        if not sync_data_result.result:
            self._logger.warning(s_queue_res_msg)
        else:
            self._logger.info(s_queue_res_msg)
        # Execute the benchmark
        self._set_registry_mode(target_mode=ERegistryMode.benchmark)
        # ensure scenario generates a valid number of clients value
        num_bench_clients = self._scenario.get_benchmark_num_clients()
        if not isinstance(num_bench_clients, int):
            raise TypeError("scenario.get_benchmark_num_clients must return an integer")
        if num_bench_clients < 1:
            raise ValueError("Number of benchmark clients must be 1 or more, "
                             "instead received from the scenario: {}".format(num_bench_clients))

        self._logger.info('Executing the benchmark')
        bench_start_result = self._registry_ctrl.ctrl_start_benchmark(
            workload_id=self._scenario.get_benchmark_workload(),
            num_workers=num_bench_clients)
        if bench_start_result.failed:
            raise RuntimeError(bench_start_result.error)
        if without_waiting:
            return None  # Don't wait for the benchmark to end
        while True:
            time.sleep(15)
            bench_status_response = self.benchmark_status()
            if bench_status_response.completed or bench_start_result.failed:
                break
        # Switch back to shuffle
        self._set_registry_mode(target_mode=ERegistryMode.shuffle)
        # Return the result
        self._scenario.process_benchmark_stats(registry_read=self._registry_read,
                                               bench_end_result=bench_status_response)
        return bench_status_response

    def benchmark_status(self) -> BenchmarkStatus:
        bench_status = self._registry_ctrl.ctrl_get_benchmark()
        if bench_status.failed:
            raise RuntimeError(bench_status.error)
        return bench_status.result

    def _initialize_action_space(self) -> None:
        """
        Initialize actions for agents
        """
        agent_nodes, fragments, obs = self._scenario.get_full_allocation_observation(registry_read=self._registry_read)
        for agent in self._scenario.get_agents():
            agent.create_action_space(agent_nodes, fragments, obs)
            self._logger.info("Agent '{}' initialized with the action space size: {}."
                              .format(agent.id, agent.get_actions()))

    def _set_registry_mode(self, target_mode: ERegistryMode) -> None:
        """
        Switch current registry mode to a target mode if needed (if it's not already in that specific mode of execution)
        :param target_mode: ERegistryMode value
        """
        get_mode_result = self._registry_ctrl.ctrl_get_mode()  # Verify that the environment is in benchmark mode
        if get_mode_result.failed:
            raise EnvironmentInitializationError(get_mode_result.error)
        if get_mode_result.result == ERegistryMode.unknown:
            raise RuntimeError("Registry is in unknown mode")
        if get_mode_result.result != target_mode:
            self._logger.info("Switching registry to %s mode, current mode: %s",
                              target_mode.name, get_mode_result.result.name)
            if target_mode == ERegistryMode.benchmark:
                set_bench_result = self._registry_ctrl.ctrl_set_benchmark_mode()
            elif target_mode == ERegistryMode.shuffle:
                set_bench_result = self._registry_ctrl.ctrl_set_shuffle_mode()
            else:
                raise RuntimeError("Illegal registry mode switch attempt")
            if set_bench_result.failed:
                raise RuntimeError(set_bench_result.error)
