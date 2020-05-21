#!/bin/sh

# Prfix for container names
PFX=${1:-""}

# With do-nothing
(cd ../docker/compositions/redis/scripts/ && sh dqn_dispose.sh ${PFX}dqn_dn)

# Without do-nothing
(cd ../docker/compositions/redis/scripts/ && sh dqn_dispose.sh ${PFX}dqn_wdn)
