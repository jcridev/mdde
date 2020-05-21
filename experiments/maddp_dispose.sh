#!/bin/sh

# Prfix for container names
PFX=${1:-""}

# With do-nothing
(cd ../docker/compositions/redis/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_dn)

# Without do-nothing
(cd ../docker/compositions/redis/scripts/ && sh maddpg_dispose.sh ${PFX}maddpg_wdn)
