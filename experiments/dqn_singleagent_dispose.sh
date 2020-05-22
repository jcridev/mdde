#!/bin/sh

# Prfix for container names
PFX=${1:-""}

COMPOSE_DIR=../docker/compositions/redis
COMMON_TAG_PFX=dqn_sa

# With do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh singleagent_dqn_dispose.sh ${PFX}${COMMON_TAG_PFX}_dn)

# Without do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh singleagent_dqn_dispose.sh ${PFX}${COMMON_TAG_PFX}_wdn)
