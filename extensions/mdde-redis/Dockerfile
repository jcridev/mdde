FROM redis:5-buster
# configure redis
COPY ./redis/redis.conf /usr/local/etc/redis/redis.conf
CMD [ "redis-server", "/usr/local/etc/redis/redis.conf" ]

# install java
ENV DEBIAN_FRONTEND noninteractive
RUN echo "deb http://deb.debian.org/debian/ sid main" >> /etc/apt/sources.list
RUN apt-get update && apt-get install -y --no-install-recommends apt-utils
RUN mkdir -p /usr/share/man/man1
RUN apt-get install openjdk-13-jdk -y
