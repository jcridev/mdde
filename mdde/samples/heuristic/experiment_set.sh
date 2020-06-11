#!/bin/sh

RESULT=${1:-"./out"}

GREEDY_RES="${RESULT}/greedy"
STARVING_RES="${RESULT}/starving"

mkdir -p "${GREEDY_RES}"
mkdir -p "${STARVING_RES}"

# Greedy agents
for IDX in $(seq 0 10)
do
    python sample_heiristic_greedy_agents.py --out-file "${GREEDY_RES}/${IDX}.log"
done

# Starving agents
for IDX in $(seq 0 10)
do
    python sample_heiristic_starving_agents.py --out-file "${STARVING_RES}/${IDX}.log"
done