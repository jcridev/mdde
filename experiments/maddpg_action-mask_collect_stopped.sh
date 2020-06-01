#!/bin/sh

# Prfix for container names
PFX=${1:-""}

WORK_DIR=$PWD
COMPOSE_DIR=../docker/compositions/redis

# With do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_dn
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_dn ${WORK_DIR}/res_maddpg/maddpg_am_dn)

# Without do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn ${WORK_DIR}/res_maddpg/maddpg_am_wdn)

# With do-nothing, gamma=0.5
#mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_dn_g05
#(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_dn_g05 ${WORK_DIR}/res_maddpg/maddpg_am_dn_g05)

# Without do-nothing, gamma=0.5
#mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn_g05
#(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn_g05 ${WORK_DIR}/res_maddpg/maddpg_am_wdn_g05)
