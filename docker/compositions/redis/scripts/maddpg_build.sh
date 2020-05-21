#!/bin/sh

# Build (re-build) default MADDPG and all supporting images

SKIPENVBASE=0
SKIPREGISTRY=0
for i in "$@"
do
case $i in
    -b|--baseenvskip) # Skip base environment image rebuild
    SKIPENVBASE=1
    shift
    ;;
    -r|--regskip) # Skip registry image rebuild
    SKIPREGISTRY=1
    shift 
    ;;
    *)
    echo "Unknown argument ${i}"
    exit 1
    ;;
esac
done

# 1. Build the MADDPG container based on the sample code
if [ $SKIPENVBASE -eq 0 ];then
  (cd ../../../images/environment/scripts/ && sh build_maddpg-ray.sh)
else
  (cd ../../../images/environment/scripts/ && sh build_maddpg-ray.sh -b)
fi

# 2. Build the MDDE Registry images
if [ $SKIPREGISTRY -eq 0 ];then
  (cd ../../../images/registry/scripts/ && sh build_redis_images.sh)
fi

# 3. Build the final composition, including MDDE Registry image (From MDDE Registry base image) relying on Redis DB for own storgate and data nodes
(cd .. && docker-compose -f docker-compose.yml -f docker-compose.maddpg.yml build --no-cache)
