#!/bin/bash

#Expected positional arguments
BROKER_ID=$1
ZOO_CONNECT=$2
PORT=$3

# https://kafka.apache.org/quickstart
# Kafka installation folder
KAFKA_HOME=/opt/kafka
KAFKA_SERVER_CFG=$KAFKA_HOME/config/server.properties

# Adjust the configuration file
sed -i 's/broker\.id=.*/broker\.id='"${BROKER_ID}"'/' "$KAFKA_SERVER_CFG"
sed -i 's/zookeeper\.connect.*/zookeeper\.connect='"${ZOO_CONNECT}"'/' "$KAFKA_SERVER_CFG"
sed -i 's/#listeners=PLAINTEXT:\/\/:.*/listeners=PLAINTEXT:\/\/:'"${PORT}"'/' "$KAFKA_SERVER_CFG"


exec "$KAFKA_HOME/bin/kafka-server-start.sh" "$KAFKA_SERVER_CFG"