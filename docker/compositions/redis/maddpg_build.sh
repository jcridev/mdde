#!/bin/sh

# Build (re-build) default MADDPG and all supporting images

# 1. Build the MADDPG container based on the sample code
docker build -t mdde/env:maddpg-ray-latest -f ../../images/environment/maddpg-ray.Dockerfile ../../../mdde --no-cache
# 2. Build the MDDE Registry base image
docker build -t mdde/registry:base-latest -f ../../images/registry/registry-base.Dockerfile ../../../registry --no-cache
# 3. Build the final composition, including MDDE Registry image (From MDDE Registry base image) relying on Redis DB for own storgate and data nodes
docker-compose -f ./docker-compose.yml -f ./docker-compose.maddpg.yml build --no-cache