#!/bin/bash

MDDE_ROOT=$1

cfg="cpu"
if [ "$#" -eq 2 ];then
    cfg="$2"
fi

if [ "$cfg" = "gpu" ];then
    pip install --progress-bar off tensorflow-gpu==1.15.5
    pip install GPUtil
elif [ "$cfg" = "cpu" ];then
    pip install --progress-bar off tensorflow==1.15.5
else
    echo "Incorrect configuration argument '$cfg'" >&2
    exit 1
fi

pip install --progress-bar off psutil==5.6.6
pip install --progress-bar off pandas==1.1.5
pip install --progress-bar off dm-tree==0.1.4
pip install --progress-bar off tensorflow-probability==0.7.0
pip install --progress-bar off tabulate==0.8.7
pip install --progress-bar off requests==2.22.0


# Install MDDE
pip install --progress-bar off -e $MDDE_ROOT/core
pip install --progress-bar off -e $MDDE_ROOT/mdde-registry-client-tcp
pip install --progress-bar off -e $MDDE_ROOT/integration-ray
