#!/bin/sh

# Project name as defined when compose was launched
PROJ=${1:-mdde_maddpg_am}

# Bring up full setup with default MADDPG
docker-compose -f ../docker-compose.yml -f ../docker-compose.maddpg-actmask.yml -p $PROJ up
