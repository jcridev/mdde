# Debug 
If you only need to spin up local instances of Redis, use the convenience script `start_redis_only.sh`.

# Execution of the experiments

The following procedure is suitable for the MADDPG experiment based on the sample code.

The scripts serve as the usage example. Inspect the contents when you want to create your own custom experimental setup.

**Note**: depending on your Docker and OS configuration, all of the following commands might require *root* privileges to execute. In this case add `sudo` to each call.

1. Build the required images: `sh maddpg_build.sh`
2. Start the execution: `maddpg_start.sh`
   * In order to run multiple instances of the compose-file on the same machine or within the same Docker swarm, you need to define a unique project name for each. The default project name is `mdde_maddpg`. If you want to specify a custom project name add it as an argument to the command like so: `sh maddpg_start.sh mdde_custom_proj`.
3. Retrieve results. **Note:** *If you started a run with a custom project name, you must supply it as the first argument as well to the following scripts. Inspect the scripts fro more details on the available arguments.*
   * **Intermediate**: If the docker containers are still running: `maddpg_retrieve_results.sh`
   * **Final**: If the containers are down: `maddpg_retrieve_results_stopped.sh`
4. After you've retrieved all of the logs and files of interest, you might want to free up space taken by the volume and the containers. For that run: `maddpg_dispose.sh`. **Note:** *If you started a run with a custom project name, you must supply it as the first argument.*

When you're done with all of the MDDE Docker related experimental runs, you might want to remove any locally created images. Refer to the official Docker documentation for the instructions on how to do it: [docker image rm](https://docs.docker.com/engine/reference/commandline/image_rm/).