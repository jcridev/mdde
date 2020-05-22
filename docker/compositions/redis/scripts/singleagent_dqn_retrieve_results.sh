#/bin/sh

# Retrieve results and logs from the currently running containers

# Project name as defined when compose was launched
PROJ=${1:-mdde_dqn_single}
# Destination folder
DEST=${2:-.}


REGISTRY_CONTAINER=${PROJ}_registry_1
DQN_CONTAINER=${PROJ}_dqn_sa_1

if [ ! "$(docker ps -q -f name=${REGISTRY_CONTAINER})" ]; then
    echo "Registy container is not running: '${REGISTRY_CONTAINER}'."
    exit 1 
fi

if [ ! "$(docker ps -q -f name=${DQN_CONTAINER})" ]; then
    echo "DQN container is not running:'${DQN_CONTAINER}'."
    exit 1 
fi

# Create a folder with a unique name
UUID=$(dbus-uuidgen)
DEST_U_FOLDER=${DEST}/${UUID}
mkdir -p ${DEST_U_FOLDER}

docker cp ${REGISTRY_CONTAINER}:/mdde/registry-logs ${DEST_U_FOLDER}
docker cp ${DQN_CONTAINER}:/mdde/results ${DEST_U_FOLDER}

echo ${UUID}
