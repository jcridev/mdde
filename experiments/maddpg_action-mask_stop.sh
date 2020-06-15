#!/bin/sh

# Prfix for container names
PFX=${1:-""}

# *.env file used by docker-compose
COMPOSE_DIR=../docker/compositions/redis


# With do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_dn)

# Without do-nothing
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_wdn)

# With do-nothing, disregard storage
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_dn_sm0)

# With do-nothing, disregard storage
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_wdn_sm0)

# With do-nothing, disregard storage
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_dn_sm0_b1)

# With do-nothing, disregard storage
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_wdn_sm0_b1)

# Without do-nothing, disregard storage, bench at every step, 1e7 replay buffer
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_wdn_sm0_b1_10mrb)

# With do-nothing, consider storage, 80 fragments
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_wdn_f80)

# With do-nothing, consider storage, 80 fragments, bench at every step
(cd ${COMPOSE_DIR}/scripts/ && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_wdn_b1_f80)


# With do-nothing, gamma=0.5
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--gamma 0.5" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_dn_g05)

# Without do-nothing, gamma=0.5
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --gamma 0.5" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_act-mask_stop.sh ${PFX}maddpg_am_wdn_g05)