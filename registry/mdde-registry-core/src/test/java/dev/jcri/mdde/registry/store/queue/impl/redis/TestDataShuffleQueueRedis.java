package dev.jcri.mdde.registry.store.queue.impl.redis;

import dev.jcri.mdde.registry.configuration.redis.RegistryStoreConfigRedis;
import dev.jcri.mdde.registry.store.impl.redis.RedisConnectionHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

public class TestDataShuffleQueueRedis {

    @Container
    public static GenericContainer redis = new GenericContainer<>("redis:5").withExposedPorts(6379);

    public static RedisConnectionHelper redisRegistryStoreConnHelper;

    @BeforeAll
    public static void setUpRedis() {
        RegistryStoreConfigRedis redisStoreConfig = new RegistryStoreConfigRedis();
        redisStoreConfig.setHost(redis.getContainerIpAddress());
        redisStoreConfig.setPort(redis.getFirstMappedPort());

        redisRegistryStoreConnHelper = new RedisConnectionHelper(redisStoreConfig);
    }

    @AfterAll
    public static void cleanup(){
        if(redisRegistryStoreConnHelper != null){
            redisRegistryStoreConnHelper.close();
        }
    }
}
