#!/bin/sh

# Prfix for container names
PFX=${1:-""}

COMPOSE_DIR=../docker/compositions/redis

# With do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_dn)

# Without do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_wdn)

# With do-nothing, disregard storage
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_dn_sm0)

# Without do-nothing, disregard storage
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_wdn_sm0)

# With do-nothing, gamma=0.5
#(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_dn_g05)

# Without do-nothing, gamma=0.5
#(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_wdn_g05)
