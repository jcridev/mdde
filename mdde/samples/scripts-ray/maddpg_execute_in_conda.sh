#!/bin/bash

source ~/miniconda/etc/profile.d/conda.sh
conda activate mdde

python $1 --result-dir $2 --temp-dir /ray_temp --reg-host "$3" --reg-port $4 --env-temp-dir /agents_temp --config "$5"
