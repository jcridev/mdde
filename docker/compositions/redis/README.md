# Compositions

Example of the docker-compose configurations, which are suitable for running MDDE experiments. 

* `docker-compose.yml` - Base containers for the Redis based infrastructure. Includes Redis backend node for the MDDE registry specific data `registry_store`, as well as four data nodes `redis_node_#` and the MDDE registry server `registry`.  Additioanlly declares two volumes: `mdde_reg_logs` for MDDE registry logs; `mdde_shared` for the MDDE registry configuration files shared with the environment.
* `docker-compose.debug.yml` - Override for the base `docker-compose.yml` that defines ports mappings for all containers. Intended for the local development and debugging of the Python-based environment without a need to manually set up all of the needed data nodes or even an MDDE registry instance. If you only need to spin up local instances of Redis, use the convenience script `start_redis_only.sh`.
* `docker-compose.maddpg.yml` - Based on the base `docker-compose.yml`, adds a container based on the MDDE MADDPG image as it's defined in the `../../images/environment`. Additionally defines a volume `mdde_results` for the results. 

# Execution of the experiments

The following procedure is suitable for the MADDPG experiment based on the sample code.

**Note**: depending on your Docker and OS configuration, all of the following commands might require *root* privileges to execute. In this case add `sudo` to each call.

1. Build the required images: `sh maddpg_build.sh`
2. Start the execution: `maddpg_start.sh`
   * In order to run multiple instances of the compose-file on the same machine or within the same Docker swarm, you need to define a unique project name for each. The default project name is `mdde_maddpg`. If you want to specify a custom project name add it as an argument to the command like so: `sh maddpg_start.sh mdde_custom_proj`.
3. Retrieve results. **Note:** *If you started a run with a custom project name, you must supply it as the first argument as well to the following scripts. Inspect the scripts fro more details on the available arguments.*
   * **Intermediate**: If the docker containers are still running: `maddpg_retrieve_results.sh`
   * **Final**: If the containers are down: `maddpg_retrieve_results_stopped.sh`