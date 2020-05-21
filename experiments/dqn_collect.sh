#!/bin/sh

# Prfix for container names
PFX=${1:-""}

WORK_DIR=$PWD

# With do-nothing
mkdir -p ${WORK_DIR}/res_dqn/dqn_dn
(cd ../docker/compositions/redis/scripts/ && sh dqn_retrieve_results.sh ${PFX}dqn_dn ${WORK_DIR}/dqn_dn)

# Without do-nothing
mkdir -p ${WORK_DIR}/res_dqn/dqn_wdn
(cd ../docker/compositions/redis/scripts/ && sh dqn_retrieve_results.sh ${PFX}dqn_wdn ${WORK_DIR}/dqn_wdn)
