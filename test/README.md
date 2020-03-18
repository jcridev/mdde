# Debug environment set up

Current directory `test` is the working directory.

Pre-requisites:
* Python 3.7 (3.8)
* Anaconda 1.7 
* OpenJDK 11
* Maven 3.6

## Linux

1. `sh prepare_test.sh`
   
2. `conda env create -f ../mdde/support/conda-p37.yml`
   
3. `sh start_redis_docker_compose.sh`
   
4. `sudo docker-compose -f ../docker/redis_nodes.docker-compose.yml up`

Leave the databases running. To shut down the Redis nodes, press Ctrl-C in the terminal window where the `up` command was executed (wuthout `-d`) or `sudo docker-compose -f ../docker/redis_nodes.docker-compose.yml down` in another.

Open another window and start the Registry server

5. `java -jar ../registry/mdde-registry/mdde-registry-tcp/target/mdde-registry-tcp.jar -p 8942 -b 8954 -c registry_config.yml`


Activate conda environment.

6. `conda activate mdde-p37`

Setup MDDE.

7. `cd ../mdde`
   
8. `pip install -e ./core`
   
9.  `pip install -e ./extensions/mdde-registry-client-tcp`
    
10. `pip install -U https://s3-us-west-2.amazonaws.com/ray-wheels/latest/ray-0.8.0.dev3-cp36-cp36m-manylinux1_x86_64.whl`
    
11. `pip install -e ./extensions/integration-ray`

12. `cd test`


Now run test configuration:

* Debug, without a learner:  `python test_environment.py`
* MADDPG: `python test_maddpg.py`