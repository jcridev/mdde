#!/bin/sh

# Project name as defined when compose was launched
PROJ=${1:-mdde_dqn}

# Bring up full setup with default DQN
docker-compose -f ../docker-compose.yml -f ../docker-compose.ray-dqn.yml -p $PROJ up
