#!/bin/sh

# Remove the project containers and volumes

# Project name as defined when compose was launched
PROJ=${1:-mdde_maddpg_am}

docker-compose -f ../docker-compose.yml -f ../docker-compose.maddpg-actmask.yml -p $PROJ down -v
