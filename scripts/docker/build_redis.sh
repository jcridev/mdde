#!/bin/sh

# build Redis based data node image
docker build -t data_node:latest ../../extensions/mdde-redis/