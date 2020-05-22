#!/bin/sh

# Prfix for container names
PFX=${1:-""}

# *.env file used by docker-compose
ARGS_FILE=args.env
COMPOSE_DIR=../docker/compositions/redis
COMMON_TAG_PFX=dqn_sa

# Build required images 
(cd ${COMPOSE_DIR}/scripts && sh singleagent_dqn_build.sh)

# With do-nothing
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh singleagent_dqn_start_detached.sh ${PFX}${COMMON_TAG_PFX}_dn)

# Without do-nothing
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "LAUNCH_ARGS=--no-do-nothing" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh singleagent_dqn_start_detached.sh ${PFX}${COMMON_TAG_PFX}_wdn)

# Cleanup the args file
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "" > ${COMPOSE_DIR}/${ARGS_FILE}