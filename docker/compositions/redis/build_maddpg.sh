#!/bin/sh

# Build (re-build) default MADDPG and all supporting images
docker build -t mdde/env:maddpg-ray-latest -f ../../images/environment/maddpg-ray.Dockerfile ../../../mdde --no-cache
docker build -t mdde/registry:base-latest -f ../../images/registry/registry-base.Dockerfile ../../../registry --no-cache
docker-compose -f ./docker-compose.yml -f ./docker-compose.maddpg.yml build --no-cache
