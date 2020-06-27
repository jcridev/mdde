#!/bin/sh

# Bring up only Redis nodes. Useful for debugging code locally
(cd .. && docker-compose -f docker-compose.yml -f docker-compose.debug.yml down -v)
