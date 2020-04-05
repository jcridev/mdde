#bin/sh

WORKING_DIR=$PWD
# Build base registy image
docker build -t mdde/registry:base-latest ../registry --no-cache
# Build redis registy image
docker build -t mdde/registry:redis-latest -f ./registry-redis.Dockerfile . --no-cache
