FROM redis:5.0.9
COPY redis.conf /etc/redis/redis.conf

CMD [ "redis-server", "/etc/redis/redis.conf" ]
