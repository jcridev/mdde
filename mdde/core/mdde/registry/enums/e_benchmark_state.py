from enum import Enum


class EBenchmarkState(Enum):
    """
    Known benchmark run states.

    Declaration source: registry/shared/mdde-registry-shared/src/main/java/dev/jcri/mdde/registry/shared/benchmark/enums/EBenchmarkRunStage.java
    """
    ready = 'ready'
    starting = 'starting'
    running = 'running'
    finalizing = 'finalizing'
    done = 'done'
