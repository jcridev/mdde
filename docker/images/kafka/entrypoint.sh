#!/bin/bash

#Expected positional arguments
BROKER_ID=$1
ZOO_CONNECT=$2
HOST=$3
PORT=$4
LOGS_DIR=$5

# https://kafka.apache.org/quickstart
# Kafka installation folder
KAFKA_HOME=/opt/kafka
KAFKA_SERVER_CFG=$KAFKA_HOME/config/server.properties

# Adjust the configuration file
sed -i 's/broker\.id=.*/broker\.id='"${BROKER_ID}"'/' "$KAFKA_SERVER_CFG"
sed -i 's/zookeeper\.connect.*/zookeeper\.connect='"${ZOO_CONNECT}"'/' "$KAFKA_SERVER_CFG"
sed -i 's/#listeners=PLAINTEXT:\/\/:.*/listeners=PLAINTEXT:\/\/'"${HOST}"':'"${PORT}"'/' "$KAFKA_SERVER_CFG"
#sed -i 's/#advertised\.listeners.*/advertised\.listeners=PLAINTEXT:\/\/'"${HOST}"':'"${PORT}"'/' "$KAFKA_SERVER_CFG"

KAFKA_LOGS_DIR=$LOGS_DIR/kafka-logs-$BROKER_ID
mkdir -p $KAFKA_LOGS_DIR
sed -i 's/log\.dirs=*/log\.dirs='"${KAFKA_LOGS_DIR}"'/' "$KAFKA_SERVER_CFG"


exec "$KAFKA_HOME/bin/kafka-server-start.sh" "$KAFKA_SERVER_CFG"