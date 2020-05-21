#!/bin/sh

# Stop the currently running containers for the specified project without disposing the volumes

# Project name as defined when compose was launched
PROJ=${1:-mdde_maddpg}

docker-compose -f ../docker-compose.yml -f ../docker-compose.maddpg.yml -p $PROJ stop
