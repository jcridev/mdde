# MDDE Registry Docker images

Build an image appropriate for your scenario.

## Base image

`registry-base.Dockerfile` is a base file for all MDDE registry images. Build it first and deploy to the local repository before building any derived images.

Note the build context: `../../../registry`

`docker build -t mdde/registry:base-latest -f ./registry-base.Dockerfile ../../../registry --no-cache`

## Derived images

Primerally aimed at creating images working with the specific data nodes.

## Redis 5

`docker build -t mdde/registry:redis-latest -f ./registry-redis.Dockerfile . --no-cache`