<!-- omit in toc -->
# Registry

The role of the registry is to provide MDDE with a "synthetic" distributed data management environment providing fine-grained control over the data allocation while not relying on the heuristics of any specific database. The registry is not containing any learner specific functionality. 

Database nodes are used as simple data storage nodes with no knowledge of each other or explicit clustering mechanisms. Registry controls the placement, replication, and retrieval of any specific data record.

Additionally, the registry provides the benchmarking capability allowing to run a series of tests on the current data distribution and returning the resulted metrics (ex. throughput).

- [Requirements](#requirements)
- [Dependencies](#dependencies)
  - [YCSB](#ycsb)
  - [Data storage](#data-storage)
  - [Libraries](#libraries)
- [Structure](#structure)
- [Build](#build)
- [Install shared dependencies](#install-shared-dependencies)
- [Run](#run)
  - [TCP Server](#tcp-server)
- [Extension](#extension)

## Requirements

* OpenJDK 11
* Maven 3.6.x

## Dependencies

### YCSB

To provide the benchmark functionality, we rely on the [YCSB](https://github.com/brianfrankcooper/YCSB) project. We provide our own [fork](https://github.com/jcridev/YCSB/tree/redis-mdde-client) of the project, where we add MDDE integration code to the original YCSB code base.

### Data storage

Current implemenation of the registry supports [Redis](https://redis.io/) as a data storage medium for the registry store itself and the data nodes. 

### Libraries

* [Netty 4](https://github.com/netty/netty) used to implement TCP API of the registry.
* [Jackson](https://github.com/FasterXML/jackson) utilized for serialization and deserialization of data requests and replies in the provided data manipulation API.
* [Log4j](https://github.com/apache/log4j) for logging.
* [Jedis](https://github.com/xetorthio/jedis) used as Redis client.
* [JUnit 5](https://github.com/junit-team/junit5) for running unit tests.
* [Testcontainers](https://github.com/testcontainers/testcontainers-java) is used to simplify unit testing where additional infrastructure is required (ex. Redis nodes).


## Structure

* `./mdde-registry` parent POM
    * `/mdde-registry-core` Core functionality of the registry
    * `/mdde-registry-tcp` TCP server
* `./shared` folder containing packages that can be shared with JAVA based projects, that are being integrated with MDDE registry
    * `/mdde-registry-shared` enums for registry commands, error codes; serialization containers
    * `/mdde-registry-tcp-shared` tcp client and serialization containers

**Note**: the shared projects are bult with the Java language level 8, while the main mdde-registry modules require the minimum language level 11.  

## Build

```
cd ./mdde-registry
mvn -Dmaven.test.skip=true clean package
```

## Install shared dependencies

If you want to install the shared dependencies into the local maven repository, you can choose whichmodules you require. For example for YCSB MDDE Redis tcp based client, you can run the following commands

```
cd ./mdde-registry
mvn clean install --non-recursive

cd ../shared/mdde-registry-shared
mvn clean install

cd ../mdde-registry-tcp-shared
mvn clean install
```

## Run

### TCP Server

Starting the server with the provided example configuration

```
java -jar ./mdde-registry/mdde-registry-tcp/target/mdde-registry-tcp.jar -p 8942 -b 8954 -c ../test/registry_config.yml
```
Parameters:
* `-p` registry control interface TCP port (read, write, control queries)
* `-b` registry benchmark TCP port
* `-c` path to the configuration file

## Extension

...