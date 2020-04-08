#!/bin/sh

# Bring up full setup with default MADDPG
docker-compose -f ./docker-compose.yml -f ./docker-compose.maddpg.yml build
