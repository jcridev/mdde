#!/bin/sh

# Prfix for container names
PFX=${1:-""}

POSTFX=_actm

WORK_DIR=$PWD
COMPOSE_DIR=../docker/compositions/redis

# With do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_dn${POSTFX}
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_retrieve_results.sh ${PFX}maddpg_dn${POSTFX} ${WORK_DIR}/res_maddpg/maddpg_dn${POSTFX})

# Without do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_wdn${POSTFX}
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_retrieve_results.sh ${PFX}maddpg_wdn${POSTFX} ${WORK_DIR}/res_maddpg/maddpg_wdn${POSTFX})
