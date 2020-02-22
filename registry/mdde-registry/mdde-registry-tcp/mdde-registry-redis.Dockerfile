# Stage 0: Build the registry jar
FROM openjdk:11 AS d-builder

ENV BUILDPATH = /usr/src/mddereg
ENV BINPATH = /opt/mddereg/bin
# Get maven
RUN apt update
RUN apt install maven -y

COPY ../mdde-registry-core BUILDPATH
WORKDIR BUILDPATH

RUN mvn package

# Stage 1: Deploy registry with Redis
FROM redis:5-buster
ENV DEBIAN_FRONTEND noninteractive
RUN mkdir -p /usr/share/man/man1
RUN apt update
RUN apt install openjdk-11-jre-headless -y

WORKDIR BINPATH
COPY --from=d-builder BUILDPATH/target/mdde-registry.jar .


# Usage example:  
# docker build -f ./mdde-registry-redis.Dockerfile -t "mdde-registry:latest" .



