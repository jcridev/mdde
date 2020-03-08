<!-- omit in toc -->
# Multi-agent Data Distribution Environment (MDDE)

MDDE is developed to facilitate the application of the reinforcement learning algorithms for optimizaiton of data distribution within a distributed data storage. 

The primary focus of this environment is read optimizatio scenarios.

## Structure

MDDE consists out a the following key components

### Environment
`./mdde` 

### Registry
`./registry`

### Workload generator (benchmark)

To provide the benchmark functionality, we rely on the [YCSB](https://github.com/brianfrankcooper/YCSB) project. We provide our own [fork](https://github.com/jcridev/YCSB/tree/redis-mdde-client) of the project, where we add MDDE integration code to the original YCSB code base.
