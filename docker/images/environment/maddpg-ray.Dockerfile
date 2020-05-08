FROM mdde/env/ray-base:latest
# CPU bound environment container for MADDPG implemented in Ray RLlib
# Context (repo root): ../../../

LABEL org.label-schema.name="mdde/env/ray-maddpg"
LABEL org.label-schema.description="MDDE Ray MADDPG"
LABEL org.label-schema.vcs-url="https://github.com/akharitonov/mdde/"
LABEL org.label-schema.version="0.5"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/akharitonov/"

# Entrypoint code
COPY $GIT_MDDE_SRC/samples/sample_ray_maddpg.py ./run.py
