# YCSB MDDE Redis client
FROM mdde/registry/base:latest

LABEL org.label-schema.name="mdde/registry/redis"
LABEL org.label-schema.description="MDDE Registry for Redis data nodes"
LABEL org.label-schema.vcs-url="https://github.com/jcridev/mdde/"
LABEL org.label-schema.version="0.7"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/jcridev/"

# Copy cofig file to the volume
ARG cofig_file=./sample_configurations/config.yml 
COPY $cofig_file $MDDE_SHARED/config.yml

# YCSB
# using a tested with the image commit (might not be the latest)
ENV YCSB_COMMIT 0912ac0bad1f9f377281d690eeb32719aaaf1817

ENV YCSB_GIT_DIR /usr/src/ycsb_git
ENV YCSB_BIN /usr/bin/ycsb
RUN git clone -n --single-branch --branch redis-mdde-client https://github.com/jcridev/YCSB.git $YCSB_GIT_DIR
WORKDIR $YCSB_GIT_DIR
RUN git checkout $YCSB_COMMIT
# Build YCSB
RUN mvn -pl site.ycsb:mdde.redis-binding -am clean package -DskipTests
# Move buid to the YCSB bin folder
COPY ./scripts/ycsb_unpack.sh .
RUN sh ycsb_unpack.sh $YCSB_GIT_DIR $YCSB_BIN
# Cleanup
RUN rm -rf $YCSB_GIT_DIR

WORKDIR $REGISTRY_BIN
