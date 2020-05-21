#!/bin/sh

# Prfix for container names
PFX=${1:-""}

WORK_DIR=$PWD

# With do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_dn
(cd ../docker/compositions/redis/scripts/ && sh maddpg_retrieve_results.sh ${PFX}maddpg_dn ${WORK_DIR}/maddpg_dn)

# Without do-nothing
mkdir -p ${WORK_DIR}/res_maddpg/maddpg_wdn
(cd ../docker/compositions/redis/scripts/ && sh maddpg_retrieve_results.sh ${PFX}maddpg_wdn ${WORK_DIR}/maddpg_wdn)
