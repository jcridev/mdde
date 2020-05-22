FROM mdde/env/ray-base:latest
# CPU bound environment container for DQN implemented in Ray RLlib
# Context (repo root): ../../../

LABEL org.label-schema.name="mdde/env/ray-dqn-single"
LABEL org.label-schema.description="MDDE Ray DQN Single-agent"
LABEL org.label-schema.vcs-url="https://github.com/akharitonov/mdde/"
LABEL org.label-schema.version="0.5"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/akharitonov/"

# Entrypoint code
COPY $GIT_MDDE_SRC/samples/sample_ray_dqn_single_agent.py ./run.py
