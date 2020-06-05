#!/bin/sh

# Project name as defined when compose was launched
PROJ=${1:-mdde_debug}

(cd .. && docker-compose -f docker-compose.yml -f docker-compose.debug.yml -p $PROJ up)
