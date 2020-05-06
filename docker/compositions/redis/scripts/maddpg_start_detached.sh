#!/bin/sh

# Project name as defined when compose was launched
PROJ=${1:-mdde_maddpg}

# Bring up full setup with default MADDPG
docker-compose -f ../docker-compose.yml -f ../docker-compose.maddpg.yml -p $PROJ up -d
