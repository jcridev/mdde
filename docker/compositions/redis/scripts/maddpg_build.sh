#!/bin/sh

# Build (re-build) default MADDPG and all supporting images

# 1. Build the MADDPG container based on the sample code
(cd ../../../images/environment/scripts/ && sh build_maddpg-ray.sh)
# 2. Build the MDDE Registry images
(cd ../../../images/registry/scripts/ && sh build_redis_images.sh)
# 3. Build the final composition, including MDDE Registry image (From MDDE Registry base image) relying on Redis DB for own storgate and data nodes
docker-compose -f ../docker-compose.yml -f ../docker-compose.maddpg.yml build --no-cache
