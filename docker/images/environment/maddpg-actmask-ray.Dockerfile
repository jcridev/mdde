FROM mdde/env/ray-base:latest
# CPU bound environment container for MADDPG implemented in Ray RLlib
# Context (repo root): ../../../

LABEL org.label-schema.name="mdde/env/ray-maddpg-am"
LABEL org.label-schema.description="MDDE Ray MADDPG"
LABEL org.label-schema.vcs-url="https://github.com/akharitonov/mdde/"
LABEL org.label-schema.version="0.5"
LABEL org.label-schema.schema-version="1.0"
LABEL maintainer="https://github.com/akharitonov/"

# Patch MADDPG
RUN rm ${CONDA_PATH}/envs/mdde/lib/python3.7/site-packages/ray/rllib/contrib/maddpg/maddpg.py
RUN rm ${CONDA_PATH}/envs/mdde/lib/python3.7/site-packages/ray/rllib/contrib/maddpg/maddpg_policy.py
RUN rm ${CONDA_PATH}/envs/mdde/lib/python3.7/site-packages/ray/rllib/models/preprocessors.py
COPY $GIT_MDDE_SRC/samples/maddpg_masked_act/maddpg.py ${CONDA_PATH}/envs/mdde/lib/python3.7/site-packages/ray/rllib/contrib/maddpg/maddpg.py
COPY $GIT_MDDE_SRC/samples/maddpg_masked_act/maddpg_policy.py ${CONDA_PATH}/envs/mdde/lib/python3.7/site-packages/ray/rllib/contrib/maddpg/maddpg_policy.py
COPY $GIT_MDDE_SRC/samples/maddpg_masked_act/preprocessors.py ${CONDA_PATH}/envs/mdde/lib/python3.7/site-packages/ray/rllib/models/preprocessors.py
# END of the patch application

# Entrypoint code
COPY $GIT_MDDE_SRC/samples/sample_ray_maddpg_act_mask.py ./run.py
