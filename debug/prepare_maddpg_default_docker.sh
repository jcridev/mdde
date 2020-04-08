#!/bin/bash

# The script is an example of build and run procedure.

DEBUG_DIR=$PWD

# Build registry images
cd ../docker/images/registry/
sh ./build_redis_images.sh

cd $DEBUG_DIR
# Build mdde images
cd ../docker/images/environment/
sh ./build_maddpg-ray.sh

cd $DEBUG_DIR
# Build the composition
cd ../docker/compositions/redis/
sh ./build_maddpg.sh

cd $DEBUG_DIR

# After it's done, run:
# cd ../docker/compositions/redis/
# sh ./start_maddpg.sh
# OR for background run:
# sh ./start_maddpg.sh -d