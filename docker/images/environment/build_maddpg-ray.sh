#!/bin/bash

docker build -t mdde/env:maddpg-ray-latest -f ./maddpg-ray.Dockerfile ../../../mdde --no-cache
