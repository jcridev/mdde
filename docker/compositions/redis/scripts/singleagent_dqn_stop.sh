#!/bin/sh

# Stop the currently running containers for the specified project without disposing the volumes

# Project name as defined when compose was launched
PROJ=${1:-mdde_dqn_single}

docker-compose -f ../docker-compose.yml -f ../docker-compose.ray-dqn-singleagent.yml -p $PROJ stop
