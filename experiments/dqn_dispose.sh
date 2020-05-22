#!/bin/sh

# Prfix for container names
PFX=${1:-""}

COMPOSE_DIR=../docker/compositions/redis

# With do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh dqn_dispose.sh ${PFX}dqn_dn)

# Without do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh dqn_dispose.sh ${PFX}dqn_wdn)
