#!/bin/bash

MDDE_ROOT=$1

cfg="cpu"
if [ "$#" -eq 2 ];then
    cfg="$2"
fi

source ~/miniconda/etc/profile.d/conda.sh
conda create -y --name mdde python=3.7
conda activate mdde

if [ "$cfg" = "gpu" ];then
    pip install --progress-bar off tensorflow-gpu==1.15.2
    pip install GPUtil
elif [ "$cfg" = "cpu" ];then
    pip install --progress-bar off tensorflow==1.15.2
else
    echo "Incorrect configuration argument '$cfg'" >&2
    exit 1
fi

pip install --progress-bar off psutil
pip install --progress-bar off pandas==1.0.3
pip install --progress-bar off dm-tree==0.1.4
pip install --progress-bar off tensorflow-probability==0.7.0
pip install --progress-bar off 'ray[rllib,tune]==0.8.4'
pip install --progress-bar off tabulate==0.8.6
pip install --progress-bar off requests


# Install MDDE
conda install -c conda-forge tiledb-py
pip install --progress-bar off -e $MDDE_ROOT/core
pip install --progress-bar off -e $MDDE_ROOT/mdde-registry-client-tcp
pip install --progress-bar off -e $MDDE_ROOT/integration-ray

conda deactivate
