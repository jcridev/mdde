#!/bin/sh

# Prfix for container names
PFX=${1:-""}

COMPOSE_DIR=../docker/compositions/redis

# With do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_dn)

# Without do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_wdn)
