# Compositions

Example of the docker-compose configurations, which are suitable for running MDDE experiments. 

* `docker-compose.yml` - Base containers for the Redis based infrastructure. Includes Redis backend node for the MDDE registry specific data `registry_store`, as well as four data nodes `redis_node_#` and the MDDE registry server `registry`.  Additioanlly declares two volumes: `mdde_reg_logs` for MDDE registry logs; `mdde_shared` for the MDDE registry configuration files shared with the environment.
* `docker-compose.debug.yml` - Override for the base `docker-compose.yml` that defines ports mappings for all containers. Intended for the local development and debugging of the Python-based environment without a need to manually set up all of the needed data nodes or even an MDDE registry instance. 
* `docker-compose.maddpg.yml` - Based on the base `docker-compose.yml`, adds a container based on the MDDE MADDPG image as it's defined in the `../../images/environment`. Additionally defines a volume `mdde_results` for the results. 

# Convenience scripts

For convenience scripts for running debug setup, experimental setup, etc. please take a look at `./scripts` folder.