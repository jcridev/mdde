# Base image for the ray-based experiments
FROM ubuntu:18.04
# CPU bound environment container for MADDPG implemented in Ray RLlib
# Context (repo root): ../../../

LABEL org.label-schema.name="mdde/env/ray-base"
LABEL org.label-schema.description="MDDE Ray base"
LABEL org.label-schema.vcs-url="https://github.com/akharitonov/mdde/"
LABEL org.label-schema.version="0.5"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/akharitonov/"

SHELL ["/bin/bash", "-c"]

RUN apt-get update

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    ca-certificates \
    cmake \
    git \
    sudo \
    rsync \
    ssh\
    wget \
    software-properties-common \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libopenmpi-dev


RUN update-ca-certificates

ENV HOME /home
WORKDIR ${HOME}/

# Download Miniconda
# https://docs.anaconda.com/anaconda/install/silent-mode/
RUN wget -q https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh -O ~/miniconda.sh && \
    chmod +x ~/miniconda.sh
RUN bash ~/miniconda.sh -b -p $HOME/miniconda
RUN rm miniconda.sh

ENV PATH ${HOME}/miniconda/bin:$PATH
ENV CONDA_PATH ${HOME}/miniconda
# https://docs.conda.io/projects/conda-build/en/latest/resources/use-shared-libraries.html
# Relying on LD_LIBRARY_PATH is not recommended but in case issues with any shred libraries, uncomment the next line
#ENV LD_LIBRARY_PATH ${CONDA_PATH}/lib:${LD_LIBRARY_PATH}

RUN eval "$(conda shell.bash hook)"

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

# Script creating the conda environment suitable for the used version of MADDPG from RLlib
COPY $GIT_MDDE_SRC/support/scripts-ray/maddpg_create_conda_env.sh ./sample_create_conda_env.sh
# Entrypoint script
COPY $GIT_MDDE_SRC/support/scripts-ray/maddpg_execute_in_conda.sh ./sample_execute_in_conda.sh
RUN chmod +x ./sample_create_conda_env.sh
RUN chmod +x ./sample_execute_in_conda.sh

# Create environment
RUN bash ./sample_create_conda_env.sh $MDDE_SRC

# Make sure conda has execution permissions
RUN find ${CONDA_PATH} -type d -exec chmod +x {} \;

# A volume for shared files, such as MDDE config.yml
ENV MDDE_RESULTS /mdde/results
RUN mkdir -p $MDDE_RESULTS
VOLUME $MDDE_RESULTS

# A volume for shared files, such as MDDE config.yml
ENV MDDE_SHARED /mdde/shared
RUN mkdir -p $MDDE_SHARED
VOLUME $MDDE_SHARED

# Run experiments
ENTRYPOINT $MDDE_SRC/sample_execute_in_conda.sh $MDDE_SRC/run.py $MDDE_RESULTS $REG_HOST $REG_PORT $MDDE_SHARED/config.yml
