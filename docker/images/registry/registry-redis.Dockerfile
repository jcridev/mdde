# YCSB MDDE Redis client
FROM mdde/registry:base-latest

LABEL org.label-schema.name="mdde/registry:redis"
LABEL org.label-schema.description="MDDE Registry for Redis data nodes"
LABEL org.label-schema.vcs-url="https://github.com/akharitonov/mdde/"
LABEL org.label-schema.version="0.5"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/akharitonov/"

# Copy cofig file to the volume
ARG cofig_file=./config.yml 
COPY $cofig_file $MDDE_SHARED/config.yml

# YCSB
# using a tested with the image commit (might not be the latest)
ENV YCSB_COMMIT eed4388f915f54038863c7c81354705985a97bf8

ENV YCSB_GIT_DIR /usr/src/ycsb_git
ENV YCSB_BIN /usr/bin/ycsb
RUN git clone -n --single-branch --branch redis-mdde-client https://github.com/akharitonov/YCSB.git $YCSB_GIT_DIR
WORKDIR $YCSB_GIT_DIR
RUN git checkout $YCSB_COMMIT
# Build YCSB
RUN mvn clean package -DskipTests
# Move buid to the YCSB bin folder
COPY ./scripts/ycsb_unpack.sh .
RUN sh ycsb_unpack.sh $YCSB_GIT_DIR $YCSB_BIN
# Cleanup
RUN rm -rf $YCSB_GIT_DIR

WORKDIR $REGISTRY_BIN
