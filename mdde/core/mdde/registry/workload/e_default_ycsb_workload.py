from enum import Enum

from mdde.registry.workload import YCSBWorkloadInfo, EYCSBWorkloadRequestDistribution


class EDefaultYCSBWorkload(Enum):
    """
    Tags of the workloads that are pre-defined in the registry.
    For more information refer to: registry/mdde-registry/mdde-registry-core/src/main/java/dev/jcri/mdde/registry/benchmark/ycsb/EYCSBWorkloadCatalog.java
    """
    READ_10000_ZIPFIAN = YCSBWorkloadInfo(tag='read10000zipfian',
                                          request_distribution=EYCSBWorkloadRequestDistribution.ZIPFIAN,
                                          read_all_fields=True,
                                          record_count=10000,
                                          operation_count=10000)

    READ_10000_LATEST = YCSBWorkloadInfo(tag='read10000zipfian',
                                         request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                         read_all_fields=True,
                                         record_count=10000,
                                         operation_count=10000)

    READ_10000_UNIFORM = YCSBWorkloadInfo(tag='read10000zipfian',
                                          request_distribution=EYCSBWorkloadRequestDistribution.UNIFORM,
                                          read_all_fields=True,
                                          record_count=10000,
                                          operation_count=10000)
