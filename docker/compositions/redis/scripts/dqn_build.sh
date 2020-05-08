#!/bin/sh

# Build (re-build) default DQN and all supporting images

# 1. Build the DQN container based on the sample code
(cd ../../../images/environment/scripts/ && sh build_dqn-ray.sh)
# 2. Build the MDDE Registry images
(cd ../../../images/registry/scripts/ && sh build_redis_images.sh)
# 3. Build the final composition, including MDDE Registry image (From MDDE Registry base image) relying on Redis DB for own storgate and data nodes
docker-compose -f ../docker-compose.yml -f ../docker-compose.ray-dqn.yml build --no-cache
