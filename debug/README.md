# Debug environment set up

Current directory `debug` is the working directory.

Pre-requisites:
* Anaconda 1.7 
* OpenJDK 11
* Maven 3.6

## Linux

1. `sh prepare_test.sh`
   
2. `conda env create -f ../mdde/support/conda-p37.yml`   
  
3. `sudo docker-compose -f ../docker/redis_nodes.docker-compose.yml up`

Leave the databases running. To shut down the Redis nodes, press Ctrl-C in the terminal window where the `up` command was executed (wuthout `-d`) or `sudo docker-compose -f ../docker/redis_nodes.docker-compose.yml down` in another.

Open another window and start the Registry server

4. `java -jar ../registry/mdde-registry/mdde-registry-tcp/target/mdde-registry-tcp.jar -p 8942 -b 8954 -c registry_config.yml`


In yet another terminal window, activate conda environment.

5. `conda activate mdde-p37`

Setup MDDE.

6. `cd ../mdde`
   
7. `pip install -e ./core`
   
8.  `pip install -e ./extensions/mdde-registry-client-tcp`
    
9. `pip install -U https://s3-us-west-2.amazonaws.com/ray-wheels/latest/ray-0.8.0.dev3-cp37-cp37m-manylinux1_x86_64.whl`
    
10. `pip install -e ./extensions/integration-ray`

11. Run debug or sample configuration
    *  Debug, without a learner:
        1.  `cd test`
        2.  `python test_environment.py`
    *  Sample:
        1.  `cd samples`
        2.  Ray RLlib MADDPG: `python test_maddpg.py` 
            1.  *[optional]* argument `--result-dir` - Path to results dir (tensorboard)
            2.  *[optional]* argument `--temp-dir` - Path to where Ray should store temp folder. Make sure it's not too long for the plasma store, otherwise ray will fail.
