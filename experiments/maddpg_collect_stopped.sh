#!/bin/sh

# Prfix for container names
PFX=${1:-""}

WORK_DIR=$PWD
COMPOSE_DIR=../docker/compositions/redis

# With do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_dn
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_retrieve_results_stopped.sh ${PFX}maddpg_dn ${WORK_DIR}/res_maddpg/maddpg_dn)

# Without do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_wdn
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_retrieve_results_stopped.sh ${PFX}maddpg_wdn ${WORK_DIR}/res_maddpg/maddpg_wdn)
