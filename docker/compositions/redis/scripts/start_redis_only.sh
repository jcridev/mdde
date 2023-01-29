#!/bin/sh

# Bring up only Redis nodes. Useful for debugging code locally
(cd .. && docker compose -f docker-compose.yml -f docker-compose.debug.yml up registry_store redis_node_0 redis_node_1 redis_node_2 redis_node_3)
