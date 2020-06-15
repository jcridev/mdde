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

# With do-nothing, disregard storage
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_dn_sm0
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_dn_sm0 ${WORK_DIR}/res_maddpg/maddpg_am_dn_sm0)

# Without do-nothing, disregard storage
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn_sm0
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn_sm0 ${WORK_DIR}/res_maddpg/maddpg_am_wdn_sm0)

# With do-nothing, disregard storage
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_dn_sm0_b1
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_dn_sm0_b1 ${WORK_DIR}/res_maddpg/maddpg_am_dn_sm0_b1)

# Without do-nothing, disregard storage
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn_sm0_b1
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn_sm0_b1 ${WORK_DIR}/res_maddpg/maddpg_am_wdn_sm0_b1)

# Without do-nothing, disregard storage, bench at every step, 1e7 replay buffer
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn_sm0_b1_10mrb
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn_sm0_b1_10mrb ${WORK_DIR}/res_maddpg/maddpg_am_wdn_sm0_b1_10mrb)

# With do-nothing, consider storage, 80 fragments
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn_f80
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn_f80 ${WORK_DIR}/res_maddpg/maddpg_am_wdn_f80)

# With do-nothing, consider storage, 80 fragments, bench at every step
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn_b1_f80
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn_b1_f80 ${WORK_DIR}/res_maddpg/maddpg_am_wdn_b1_f80)


# With do-nothing, gamma=0.5
#mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_dn_g05
#(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_dn_g05 ${WORK_DIR}/res_maddpg/maddpg_am_dn_g05)

# Without do-nothing, gamma=0.5
#mkdir -p ${WORK_DIR}/res_maddpg/maddpg_am_wdn_g05
#(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_retrieve_results_stopped.sh ${PFX}maddpg_am_wdn_g05 ${WORK_DIR}/res_maddpg/maddpg_am_wdn_g05)
