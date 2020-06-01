#!/bin/bash

SKIPBASE=0
for i in "$@"
do
case $i in
    -b|--baseskip)
    SKIPBASE=1
    shift 
    ;;
    *)
    echo "Unknown argument ${i}"
    exit 1
    ;;
esac
done

# Build base MDDE-RAY image
if [ $SKIPBASE -eq 0 ];then
  docker build -t mdde/env/ray-base:latest -f ../base-ray.Dockerfile ../../../../mdde --no-cache
fi
# Build MDDE-RAY-MADDPG image
docker build -t mdde/env/ray-maddpg:latest -f ../maddpg-ray.Dockerfile ../../../../mdde --no-cache
