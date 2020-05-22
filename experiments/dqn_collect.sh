#!/bin/sh

# Prfix for container names
PFX=${1:-""}

WORK_DIR=$PWD
COMPOSE_DIR=../docker/compositions/redis

# With do-nothing
mkdir -p ${WORK_DIR}/res_dqn/dqn_dn
(cd ${COMPOSE_DIR}/scripts/ && sh dqn_retrieve_results.sh ${PFX}dqn_dn ${WORK_DIR}/res_dqn/dqn_dn)

# Without do-nothing
mkdir -p ${WORK_DIR}/res_dqn/dqn_wdn
(cd ${COMPOSE_DIR}/scripts/ && sh dqn_retrieve_results.sh ${PFX}dqn_wdn ${WORK_DIR}/res_dqn/dqn_wdn)
