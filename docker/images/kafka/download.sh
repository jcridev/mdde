#!/bin/sh

# Download the specified version of Kafka
# Requires wget, jq

# Arguments
KAFKA_VERSION=$1
KAFKA_DOWNLOAD_FILE=$2

# Presumably faster to download from the suggested mirror:
# https://www.apache.org/dyn/closer.cgi?path=/kafka/2.4.0/kafka_2.12-2.4.0.tgz&as_json=1
# https://stedolan.github.io/jq/manual/
KAFKA_DOWNLOAD_URL=$(curl "https://www.apache.org/dyn/closer.cgi?path=/kafka/${KAFKA_VERSION}/${KAFKA_DOWNLOAD_FILE}&as_json=1" | jq -r '"\(.preferred)\(.path_info)"')
# If the mirror download is not possible, download from Apache directly (presumably slower)
# https://archive.apache.org/dist/kafka/
if wget -q --method=HEAD ${KAFKA_DOWNLOAD_URL}; then echo Downloading Kafka from a mirror ; else KAFKA_DOWNLOAD_URL="https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/${KAFKA_DOWNLOAD_FILE}" ; fi
wget -q "${KAFKA_DOWNLOAD_URL}" -O "/tmp/${KAFKA_DOWNLOAD_FILE}"