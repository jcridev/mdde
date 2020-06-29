#!/bin/sh

# Prfix for container names
PFX=${1:-""}

# Simulation (benchmark estimation flag)
SIM=" --sim"

SLEEP_BETWEEN=1
SLEEP_TIME=4m

# *.env file used by docker-compose
ARGS_FILE=args.env
COMPOSE_DIR=../docker/compositions/redis

# Build required images 
(cd ${COMPOSE_DIR}/scripts && sh maddpg_build.sh)

# With do-nothing
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#if [ "$SIM" = " --sim" ]
#then
#  echo "LAUNCH_ARGS=--sim" > ${COMPOSE_DIR}/${ARGS_FILE}
#else
#  echo "" > ${COMPOSE_DIR}/${ARGS_FILE}
#fi
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   Without do-nothing
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   With do-nothing, disregard storage
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--store-m 0.0${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_sm0)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   With do-nothing, disregard storage
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --store-m 0.0${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_sm0)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   With do-nothing, disregard storage, bench at every step
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--store-m 0.0 --bench-psteps 1${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_sm0_b1)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   Without do-nothing, disregard storage, bench at every step
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --store-m 0.0 --bench-psteps 1${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_sm0_b1)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   Without do-nothing, disregard storage, bench at every step
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --store-m 0.0 --bench-psteps 1 --buffer_size 10000000${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_sm0_b1_10mrb)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   Without do-nothing, consider storage, 80 fragments
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --n-frags 80${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_f80)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   Without do-nothing, consider storage, 80 fragments, bench at every step
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --bench-psteps 1 --n-frags 80${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_b1_f80)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   With do-nothing, consider storage, bench at every step, 10000 episodes per 101 step
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--bench-psteps 1 --num_episodes 10000 --ep_len 101${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_b1_e10k_s100)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi

#   Without do-nothing, consider storage, bench at every step, 10000 episodes per 101 step
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --bench-psteps 1 --num_episodes 10000 --ep_len 101${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_b1_e10k_s100)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   With do-nothing, consider storage, bench at every step,  10000 episodes per 101 step, ignore conflicts
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--bench-psteps 1 --num_episodes 10000 --ep_len 101 --ok-conf-a${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_b1_e10k_s100_ai)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi

#   Without do-nothing, consider storage, bench at every step, 10000 episodes per 101 step, ignore conflicts
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --bench-psteps 1 --num_episodes 10000 --ep_len 101 --ok-conf-a${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_b1_e10k_s100_ai)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi

#   Without do-nothing, scale rewards
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --scale-rew${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_scale)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi

#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --hidden-dim 960${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_dm940)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi

#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --hidden-dim 480${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_dm480)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi

rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "LAUNCH_ARGS=--no-do-nothing --bench-psteps 250 --learning_starts 2000000 --num_episodes 60000${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_long_learn)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi

#   Without do-nothing, consider storage, 80 fragments, bench at every step, bench at every step, batch size 1000, train batch 4000
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --bench-psteps 1 --n-frags 80 --smpl-batch-s 1000 --trn-batch-s 4000${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_b1_f80_stm)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   With do-nothing, consider storage, 20 fragments, bench at every step, bench at every step, batch size 1000, train batch 4000
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--bench-psteps 1 --smpl-batch-s 1000 --trn-batch-s 4000${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_b1_stm)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   Without do-nothing, consider storage, 20 fragments, bench at every step, bench at every step, batch size 1000, train batch 4000
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --bench-psteps 1 --smpl-batch-s 1000 --trn-batch-s 4000${SIM}" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_b1_stm)

#if [ $SLEEP_BETWEEN -eq 1 ]; then sleep $SLEEP_TIME; fi 

#   With do-nothing, gamma=0.5
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--gamma 0.5" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_dn_g05)

#   Without do-nothing, gamma=0.5
#rm ${COMPOSE_DIR}/${ARGS_FILE}
#echo "LAUNCH_ARGS=--no-do-nothing --gamma 0.5" > ${COMPOSE_DIR}/${ARGS_FILE}
#(cd ${COMPOSE_DIR}/scripts && sh maddpg_start_detached.sh ${PFX}maddpg_wdn_g05)

#   Cleanup the args file
rm ${COMPOSE_DIR}/${ARGS_FILE}
echo "" > ${COMPOSE_DIR}/${ARGS_FILE}