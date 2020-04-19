#!/bin/bash

MDDE_ROOT=$1

cfg="cpu"
if [ "$#" -eq 2 ];then
    cfg="$2"
fi

source ~/miniconda/etc/profile.d/conda.sh
conda create -y --name mdde python=3.7
conda activate mdde

#pip install --progress-bar off numpy==1.18.1
if [ "$cfg" = "gpu" ];then
    pip install --progress-bar off tensorflow-gpu==1.13.2
elif [ "$cfg" = "cpu" ];then
    pip install --progress-bar off tensorflow==1.13.2
else
    echo "Incorrect configuration argument '$cfg'" >&2
    exit 1
fi

pip install --progress-bar off psutil
pip install --progress-bar off tabulate==0.8.6
pip install --progress-bar off -U https://s3-us-west-2.amazonaws.com/ray-wheels/latest/ray-0.8.0.dev3-cp37-cp37m-manylinux1_x86_64.whl
pip install --progress-bar off requests


# Install MDDE
conda install -c conda-forge tiledb-py
pip install --progress-bar off -e $MDDE_ROOT/core
pip install --progress-bar off -e $MDDE_ROOT/mdde-registry-client-tcp
pip install --progress-bar off -e $MDDE_ROOT/integration-ray

conda deactivate
