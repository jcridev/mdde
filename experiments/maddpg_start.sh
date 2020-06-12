#!/bin/sh

# Prfix for container names
PFX=${1:-""}

# Simulation (benchmark estimation flag)
SIM=" --sim"

SLEEP_BETWEEN=1

# *.env file used by docker-compose
ARGS_FILE=args.env
COMPOSE_DIR=../docker/compositions/redis

# Build required images 
(cd ${COMPOSE_DIR}/scripts && sh maddpg_build.sh)

# With do-nothing
rm ${COMPOSE_DIR}/${ARGS_FILE}
if [ "$SIM" == " --sim" ]
then
  echo "LAUNCH_ARGS=--sim" > ${COMPOSE_DIR}/${ARGS_FILE}
else
  echo "" > ${COMPOSE_DIR}/${ARGS_FILE}
fi
(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn)

if [ $SLEEP_BETWEEN == 1 ]; then sleep 5m; fi 

# Without do-nothing
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "LAUNCH_ARGS=--no-do-nothing${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn).

if [ $SLEEP_BETWEEN == 1 ]; then sleep 5m; fi 

# With do-nothing, disregard storage
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "LAUNCH_ARGS=--store-m 0.0${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_sm0)

if [ $SLEEP_BETWEEN == 1 ]; then sleep 5m; fi 

# With do-nothing, disregard storage
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "LAUNCH_ARGS=--no-do-nothing --store-m 0.0${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_sm0)

if [ $SLEEP_BETWEEN == 1 ]; then sleep 5m; fi 

# With do-nothing, disregard storage
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "LAUNCH_ARGS=--store-m 0.0 --bench-psteps 1${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_sm0_b1)

if [ $SLEEP_BETWEEN == 1 ]; then sleep 5m; fi 

# With do-nothing, disregard storage
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "LAUNCH_ARGS=--no-do-nothing --store-m 0.0 --bench-psteps 1${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_sm0_b1)

if [ $SLEEP_BETWEEN == 1 ]; then sleep 5m; fi 

# With do-nothing, gamma=0.5
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--gamma 0.5" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_g05)

# Without do-nothing, gamma=0.5
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --gamma 0.5" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_g05)

# Cleanup the args file
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "" > ${COMPOSE_DIR}/${ARGS_FILE}