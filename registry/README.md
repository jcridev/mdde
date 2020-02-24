# Registry

## Requirements

* OpenJDK 11
* Maven 3.6.x

## Structure

* `./mdde-registry` parent POM
    * `/mdde-registry-core` Core functionality of the registry
    * `/mdde-registry-tcp` TCP server
* `./shared` folder containing packages that can be shared with JAVA based projects, that are being integrated with MDDE registry
    * `/mdde-registry-shared` enums for registry commands, error codes; serialization containers
    * `/mdde-registry-tcp-shared` tcp client and serialization containers


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
