#bin/sh

# Build base registy image
docker build -t mdde/registry:base-latest -f ./registry-base.Dockerfile ../../../registry --no-cache
# Build redis registy image
docker build -t mdde/registry:redis-latest -f ./registry-redis.Dockerfile . --no-cache
