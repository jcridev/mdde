#!/bin/sh


(cd ../../../images/registry/scripts/ && sh build_redis_images.sh)

# 3. Build the final composition, including MDDE Registry image (From MDDE Registry base image) relying on Redis DB for own storgate and data nodes
(cd .. && docker-compose -f docker-compose.yml -f docker-compose.debug.yml build --no-cache)
