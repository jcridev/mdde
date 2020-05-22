#!/bin/sh

# Prfix for container names
PFX=${1:-""}

WORK_DIR=$PWD
COMPOSE_DIR=../docker/compositions/redis
COMMON_TAG_PFX=dqn_sa

# With do-nothing
mkdir -p ${WORK_DIR}/res_${COMMON_TAG_PFX}/${COMMON_TAG_PFX}_dn
(cd ${COMPOSE_DIR}/scripts/ && sh singleagent_dqn_retrieve_results_stopped.sh ${PFX}${COMMON_TAG_PFX}_dn ${WORK_DIR}/res_${COMMON_TAG_PFX}/${COMMON_TAG_PFX}_dn)

# Without do-nothing
mkdir -p ${WORK_DIR}/res_${COMMON_TAG_PFX}/${COMMON_TAG_PFX}_wdn
(cd ${COMPOSE_DIR}/scripts/ && sh singleagent_dqn_retrieve_results_stopped.sh ${PFX}${COMMON_TAG_PFX}_wdn ${WORK_DIR}/res_${COMMON_TAG_PFX}/${COMMON_TAG_PFX}_wdn)
