# Base image for the ray-based experiments
FROM python:3.7.9-buster
# If switched to `alpine` or `slim` variations, install GCC manually in the container or use multi-stage builds if the final size of the container is an issue.

# CPU bound environment container for algorithms implemented in Ray RLlib
# Context (repo root): ../../../

LABEL org.label-schema.name="mdde/env/ray-base"
LABEL org.label-schema.description="MDDE Ray base"
LABEL org.label-schema.vcs-url="https://github.com/akharitonov/mdde/"
LABEL org.label-schema.version="0.8"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/akharitonov/"

ENV MDDE_SRC /usr/src/mdde

RUN mkdir p $MDDE_SRC

# Location of mdde environment source in context
ARG GIT_MDDE_SRC=.

WORKDIR $MDDE_SRC
# MDDE Core
COPY $GIT_MDDE_SRC/core ./core
# TCP Extansion
COPY $GIT_MDDE_SRC/extensions/mdde-registry-client-tcp ./mdde-registry-client-tcp
# Ray extension
COPY $GIT_MDDE_SRC/extensions/integration-ray ./integration-ray

# Script preparing the environment suitable for the used version of MADDPG from RLlib
COPY $GIT_MDDE_SRC/support/scripts-ray/sample_create_env.sh ./sample_create_env.sh
# Entrypoint script
COPY $GIT_MDDE_SRC/support/scripts-ray/sample_execute.sh ./sample_execute.sh
RUN chmod +x ./sample_create_env.sh
RUN chmod +x ./sample_execute.sh

# Create environment
RUN bash ./sample_create_env.sh $MDDE_SRC

# A volume for shared files, such as MDDE config.yml
ENV MDDE_RESULTS /mdde/results
RUN mkdir -p $MDDE_RESULTS
VOLUME $MDDE_RESULTS

# A volume for shared files, such as MDDE config.yml
ENV MDDE_SHARED /mdde/shared
RUN mkdir -p $MDDE_SHARED
VOLUME $MDDE_SHARED

# Run experiments
ENTRYPOINT $MDDE_SRC/sample_execute.sh $MDDE_SRC/run.py $MDDE_RESULTS $REG_HOST $REG_PORT $MDDE_SHARED/config.yml $LAUNCH_ARGS
