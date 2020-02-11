FROM openjdk:8u242-jre

# Required ENV variables:
# $BROKER_ID=0
# $ZOO_CONNECT=localhost:2181
# $HOST=192.168.0.1
# $PORT=9092

# https://www.apache.org/dyn/closer.cgi?path=/kafka/2.4.0/kafka_2.12-2.4.0.tgz
ENV KAFKA_VERSION=2.4.0
ENV SCALA_VERSION=2.12

LABEL org.label-schema.name="mdde/kafka"
LABEL org.label-schema.description="Apache Kafka"
LABEL org.label-schema.vcs-url="https://github.com/jcridev/mdde/docker/kafka"
LABEL org.label-schema.version="${SCALA_VERSION}_${KAFKA_VERSION}"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/jcridev/"
 
ENV SCRIPTSPATH /usr/bin

RUN apt update \
    && apt upgrade -y
RUN apt install -y wget jq

WORKDIR $SCRIPTSPATH

COPY download.sh .
COPY entrypoint.sh .
RUN chmod a+x download.sh
RUN chmod a+x entrypoint.sh

ENV KAFKA_FOLDER="kafka_${SCALA_VERSION}-${KAFKA_VERSION}"
ENV KAFKA_DOWNLOAD_FILE="${KAFKA_FOLDER}.tgz"

# Download kafka binaries archive to /tmp
RUN download.sh $KAFKA_VERSION $KAFKA_DOWNLOAD_FILE
RUN rm -f download.sh
# Unpack and move to kafka working folder
RUN tar xfz /tmp/$KAFKA_DOWNLOAD_FILE -C /opt
RUN rm -f /tmp/$KAFKA_DOWNLOAD_FILE
RUN mv /opt/$KAFKA_FOLDER /opt/kafka
WORKDIR /opt/kafka


ENV LOGS_DIR="/kafka"
VOLUME [ "${LOGS_DIR}" ]

ENTRYPOINT /usr/bin/entrypoint.sh $BROKER_ID $ZOO_CONNECT $HOST $PORT $LOGS_DIR