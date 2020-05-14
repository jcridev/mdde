# Debug environment set up

The current directory `debug` is the working directory.

Pre-requisites:
* Anaconda 1.7 
* OpenJDK 11
* Maven 3.6

## Linux

1. `sh prepare_test.sh`
   
2. `conda env create -f ../mdde/support/conda-p37.yml`
   * To *remove* the environment or its old version, run `conda env remove --name mdde-p37 --all` 
  
3. ``` 
   cd ../docker/compositions/redis
   sh start_redis_only.sh
   cd ../../../debug
   ```

Leave the databases running. To shut down the Redis nodes, press Ctrl-C in the terminal window where the `up` command was executed or `sudo docker-compose -f ../docker/compositions/redis/docker-compose.debug.yml down` in another.

Open another window and start the Registry server.

1. `java -jar ../registry/mdde-registry/packaged/mdde-registry-tcp.jar -p 8942 -b 8954 -c registry_config.yml`


In yet another terminal window, activate conda environment.

5. `conda activate mdde-p37`

Setup MDDE.

6. `cd ../mdde`
   
7. `pip install -e ./core`
   
8.  `pip install -e ./extensions/mdde-registry-client-tcp`    

9. `pip install -e ./extensions/integration-ray`
   * You might need to additionally install TensorFlow `pip install tensorflow==1.15.2'`. Currently it's not installed by default to simplify experimental environments configuration (CPU or GPU). 

10. Run debug **or** sample configuration
    *  Debug, without a learner:
        1.  `cd test`
        2.  `python test_environment.py`
    *  Sample:
        1.  `cd samples`
        2.  Ray RLlib MADDPG: `python sample_ray_maddpg.py` 
            1.  *[optional]* argument `--result-dir` - Path to results dir (tensorboard)
            2.  *[optional]* argument `--temp-dir` - Path to where Ray should store temp folder. Make sure it's not too long for the plasma store, otherwise ray will fail.
