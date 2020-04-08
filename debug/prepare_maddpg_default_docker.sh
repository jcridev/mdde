#!/bin/bash

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
cd ../docker/composition/redis/
sh ./build_maddpg.sh

cd $DEBUG_DIR

# After it's done, run:
# sh ../docker/compositions/redis/start_maddpg.sh
# OR for background run:
# sh ../docker/compositions/redis/start_maddpg.sh -d