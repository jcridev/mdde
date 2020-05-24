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

    READ_10000_LATEST = YCSBWorkloadInfo(tag='read10000latest',
                                         request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                         read_all_fields=True,
                                         record_count=10000,
                                         operation_count=10000)

    READ_10000_UNIFORM = YCSBWorkloadInfo(tag='read10000uniform',
                                          request_distribution=EYCSBWorkloadRequestDistribution.UNIFORM,
                                          read_all_fields=True,
                                          record_count=10000,
                                          operation_count=10000)

    READ_100000_ZIPFIAN = YCSBWorkloadInfo(tag='read100000zipfian',
                                           request_distribution=EYCSBWorkloadRequestDistribution.ZIPFIAN,
                                           read_all_fields=True,
                                           record_count=100000,
                                           operation_count=100000)

    READ_100000_LATEST = YCSBWorkloadInfo(tag='read100000latest',
                                          request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                          read_all_fields=True,
                                          record_count=100000,
                                          operation_count=100000)

    READ_100000_UNIFORM = YCSBWorkloadInfo(tag='read100000uniform',
                                           request_distribution=EYCSBWorkloadRequestDistribution.UNIFORM,
                                           read_all_fields=True,
                                           record_count=100000,
                                           operation_count=100000)

    READ_10000_1000_LATEST = YCSBWorkloadInfo(tag='read10000o1000latest',
                                              request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                              read_all_fields=True,
                                              record_count=10000,
                                              operation_count=1000)

    READ_100000_1000_LATEST = YCSBWorkloadInfo(tag='read100000o1000latest',
                                               request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                               read_all_fields=True,
                                               record_count=100000,
                                               operation_count=1000)

    READ_1000000_1000_LATEST = YCSBWorkloadInfo(tag='read1000000o1000latest',
                                                request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                                read_all_fields=True,
                                                record_count=1000000,
                                                operation_count=1000)

    READ_2000000_1000_LATEST = YCSBWorkloadInfo(tag='read2000000o1000latest',
                                                request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                                read_all_fields=True,
                                                record_count=2000000,
                                                operation_count=1000)

    READ_1000_10000_LATEST = YCSBWorkloadInfo(tag='read1000o10000latest',
                                              request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                              read_all_fields=True,
                                              record_count=1000,
                                              operation_count=10000)

    READ_1000_100000_LATEST = YCSBWorkloadInfo(tag='read1000o100000latest',
                                               request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                               read_all_fields=True,
                                               record_count=1000,
                                               operation_count=100000)

    READ_10000_100000_LATEST = YCSBWorkloadInfo(tag='read10000o100000latest',
                                                request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                                read_all_fields=True,
                                                record_count=10000,
                                                operation_count=100000)

    READ_10000_1000000_LATEST = YCSBWorkloadInfo(tag='read10000o1000000latest',
                                                 request_distribution=EYCSBWorkloadRequestDistribution.LATEST,
                                                 read_all_fields=True,
                                                 record_count=10000,
                                                 operation_count=1000000)
