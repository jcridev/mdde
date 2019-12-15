package dev.jcri.mdde.registry.store;

import dev.jcri.mdde.registry.store.exceptions.ReadOperationException;
import dev.jcri.mdde.registry.store.impl.redis.ConfigRedis;
import dev.jcri.mdde.registry.store.impl.redis.ReadCommandHandlerRedis;

import dev.jcri.mdde.registry.store.response.serialization.ResponseSerializerPassThrough;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RedisTests {
    /**
     * Creates a reader class that opens a connection in the constructor with the default localhost Redis instance
     * (assuming Redis is running within the system is where this test is executed)
     */
    @Test
    public void testLocalhostConnection(){
        var testConfig = new ConfigRedis();
        var serializer = new ResponseSerializerPassThrough();
        var redisReader = new ReadCommandHandlerRedis<Object>(serializer, testConfig);
        try {
            var result = redisReader.getFragmentTuples("TEST");
            assertNotNull(result);
        } catch (ReadOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
