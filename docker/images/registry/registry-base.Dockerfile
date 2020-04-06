# Base registry image
# Context: ../../../registry
FROM maven:3.6.3-jdk-11

LABEL org.label-schema.name="mdde/registry:base"
LABEL org.label-schema.description="MDDE Registry"
LABEL org.label-schema.vcs-url="https://github.com/akharitonov/mdde/"
LABEL org.label-schema.version="0.5"
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.docker.cmd="docker run -d -p 8942:8942 -p 8954:8954 mdde/registry"
LABEL maintainer="https://github.com/akharitonov/"

ENV CONTROL_PORT 8942
ENV BENCHMARK_PORT 8954

EXPOSE $CONTROL_PORT/tcp
EXPOSE $BENCHMARK_PORT/tcp
# A volume for shared files, such as MDDE config.yml
ENV MDDE_SHARED /mdde/shared
RUN mkdir -p $MDDE_SHARED
VOLUME $MDDE_SHARED
# A volume for
ENV REGISTRY_LOGS /mdde/registry-logs
RUN mkdir -p $REGISTRY_LOGS
VOLUME $REGISTRY_LOGS

# Configure registry
ENV REGISTRY_SRC /usr/src/registry
ENV REGISTRY_BIN /usr/bin/registry

COPY . $REGISTRY_SRC
# Build MDDE Registry
WORKDIR $REGISTRY_SRC/mdde-registry
RUN mvn -Dmaven.test.skip=true clean package
RUN mvn clean install --non-recursive
# Shared lib common
WORKDIR $REGISTRY_SRC/mdde-registry/shared/mdde-registry-shared
RUN mvn clean install -DskipTests
# Shared lib for TCP clients (YCSB)
WORKDIR $REGISTRY_SRC/mdde-registry/shared/mdde-registry-tcp-shared
RUN mvn clean install -DskipTests
RUN mv $REGISTRY_SRC/mdde-registry/packaged $REGISTRY_BIN

# Cleanup
RUN rm -rf $REGISTRY_SRC

CMD exec java -DmddeRegLogDir="$REGISTRY_LOGS" -jar $REGISTRY_BIN/mdde-registry-tcp.jar -p $CONTROL_PORT -b $BENCHMARK_PORT -c $MDDE_SHARED/config.yml
