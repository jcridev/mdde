package dev.jcri.mdde.registry.data.impl.redis;

import dev.jcri.mdde.registry.data.IDataShuffler;
import dev.jcri.mdde.registry.shared.configuration.DBNetworkNodesConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class TestRedisDataShuffler {

    @Container
    public static GenericContainer redis1 = new GenericContainer<>("redis:5").withExposedPorts(6379);
    @Container
    public static GenericContainer redis2 = new GenericContainer<>("redis:5").withExposedPorts(6379);

    public static List<DBNetworkNodesConfiguration> testRedisNodes;
    public static Map<String, JedisPool> redisConnections;

    /**
     * Configure test nodes and connections
     */
    @BeforeAll
    public static void setUpRedis() {
        String redis1Address = redis1.getContainerIpAddress();
        Integer redis1Port = redis1.getFirstMappedPort();
        DBNetworkNodesConfiguration redis1NetConfig = new DBNetworkNodesConfiguration();
        redis1NetConfig.setNodeId("Test-Redis-One");
        redis1NetConfig.setHost(redis1Address);
        redis1NetConfig.setPort(redis1Port);

        String redis2Address = redis2.getContainerIpAddress();
        Integer redis2Port = redis2.getFirstMappedPort();
        DBNetworkNodesConfiguration redis2NetConfig = new DBNetworkNodesConfiguration();
        redis2NetConfig.setNodeId("Test-Redis-Two");
        redis2NetConfig.setHost(redis2Address);
        redis2NetConfig.setPort(redis2Port);

        testRedisNodes= new LinkedList<>();
        testRedisNodes.add(redis1NetConfig);
        testRedisNodes.add(redis2NetConfig);

        redisConnections = new HashMap<>();
        for(var dataNode: testRedisNodes){
            var configPool = new JedisPoolConfig();
            if (dataNode.getPassword() != null) {
                redisConnections.put(dataNode.getNodeId(), new JedisPool(configPool,
                        dataNode.getHost(),
                        dataNode.getPort(),
                        2000,
                        new String(dataNode.getPassword())));
            }
            else{
                redisConnections.put(dataNode.getNodeId(), new JedisPool(configPool,
                        dataNode.getHost(),
                        dataNode.getPort(),
                        2000));
            }
        }
    }

    /**
     * Close the connections after all of the tests are done
     */
    @AfterAll
    public static void cleanup(){
        testRedisNodes = null;

        for(var testRedisConnection: redisConnections.values()){
            testRedisConnection.close();
        }
    }

    /**
     * After each test, flush the nodes
     */
    @AfterEach
    public void instancesCleanup(){
        for(var testRedisConnection: redisConnections.values()){
            try(Jedis jedis = testRedisConnection.getResource()){
                jedis.flushAll();
            }
        }
    }

    /**
     * Test Redis shuffler HASH copies
     */
    @Test
    public void testCopyHashTuples(){
        final int numberOfTestHashKeys = 100;
        final int numberOfItemsPerHash = 20;
        Map<String, Map<String, String>> testItems = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);

        // Populate node
        var connectionKeys = redisConnections.keySet().toArray(new String[0]);
        var sourceNodeKey = connectionKeys[0];
        var destinationNodeKey = connectionKeys[1];
        var sourceNode = redisConnections.get(sourceNodeKey);
        var destinationNode = redisConnections.get(destinationNodeKey);

        populateRedisWithTestDataHash(testItems, sourceNode);

        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        var copyResult = shuffler.copyTuples(sourceNodeKey, destinationNodeKey, testItems.keySet());
        assertEquals(testItems.keySet(), copyResult.getProcessedKeys());

        Set<String> destinationResKeys;
        try(Jedis jedis = destinationNode.getResource()){
            destinationResKeys = jedis.keys("*");
        }
        assertEquals(testItems.keySet(), destinationResKeys);

        assertCopyResultsHash(testItems, destinationNode);
    }

    /**
     * Test Redis shuffler SET copies
     */
    @Test
    public void testCopySetTuples(){
        final int numberOfTestSetKeys = 100;
        final int numberOfItemsPerSet = 20;
        Map<String, Set<String>> testItems = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);

        // Populate node
        var connectionKeys = redisConnections.keySet().toArray(new String[0]);
        var sourceNodeKey = connectionKeys[0];
        var destinationNodeKey = connectionKeys[1];
        var sourceNode = redisConnections.get(sourceNodeKey);
        var destinationNode = redisConnections.get(destinationNodeKey);

        populateRedisWithTestDataSet(testItems, sourceNode);

        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        var copyResult = shuffler.copyTuples(sourceNodeKey, destinationNodeKey, testItems.keySet());
        assertEquals(testItems.keySet(), copyResult.getProcessedKeys());

        Set<String> destinationResKeys;
        try(Jedis jedis = destinationNode.getResource()){
            destinationResKeys = jedis.keys("*");
        }
        assertEquals(testItems.keySet(), destinationResKeys);

        assertCopyResultsSet(testItems, destinationNode);
    }

    /**
     * Test Redis shuffler LIST copies
     */
    @Test
    public void testCopyListTuples(){
        final int numberOfTestListKeys = 100;
        final int numberOfItemsPerList = 20;
        Map<String, List<String>> testItems = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);

        // Populate node
        var connectionKeys = redisConnections.keySet().toArray(new String[0]);
        var sourceNodeKey = connectionKeys[0];
        var destinationNodeKey = connectionKeys[1];
        var sourceNode = redisConnections.get(sourceNodeKey);
        var destinationNode = redisConnections.get(destinationNodeKey);

        populateRedisWithTestDataList(testItems, sourceNode);

        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        var copyResult = shuffler.copyTuples(sourceNodeKey, destinationNodeKey, testItems.keySet());

        assertEquals(testItems.keySet(), copyResult.getProcessedKeys());

        Set<String> destinationResKeys;
        try(Jedis jedis = destinationNode.getResource()){
            destinationResKeys = jedis.keys("*");
        }
        assertEquals(testItems.keySet(), destinationResKeys);

        assertCopyResultsList(testItems, destinationNode);
    }

    /**
     * Test Redis shuffler STRING copies
     */
    @Test
    public void testCopyStringTuples(){
        final int numberOfTestStringKeys = 100;
        Map<String, String> testItems = generateTestDataString(numberOfTestStringKeys);

        // Populate node
        var connectionKeys = redisConnections.keySet().toArray(new String[0]);
        var sourceNodeKey = connectionKeys[0];
        var destinationNodeKey = connectionKeys[1];
        var sourceNode = redisConnections.get(sourceNodeKey);
        var destinationNode = redisConnections.get(destinationNodeKey);
        populateRedisWithTestDataString(testItems, sourceNode);


        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        var copyResult = shuffler.copyTuples(sourceNodeKey, destinationNodeKey, testItems.keySet());

        assertEquals(testItems.keySet(), copyResult.getProcessedKeys());

        Set<String> destinationResKeys;
        try(Jedis jedis = destinationNode.getResource()){
            destinationResKeys = jedis.keys("*");
        }
        assertEquals(testItems.keySet(), destinationResKeys);

        assertCopyResultsString(testItems, destinationNode);
    }

     /**
     * Test Redis shuffler STRING,LIST,HASH,SET copies
     */
    @Test
    public void testCopyMixedTuples(){
        final int numberOfTestStringKeys = 100;
        final int numberOfTestListKeys = 100;
        final int numberOfItemsPerList = 20;
        final int numberOfTestSetKeys = 100;
        final int numberOfItemsPerSet = 20;
        final int numberOfTestHashKeys = 100;
        final int numberOfItemsPerHash = 20;

        Map<String, Map<String, String>> testHashItems = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);
        Map<String, Set<String>> testSetItems = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);
        Map<String, List<String>> testListItems = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);
        Map<String, String> testStringItems = generateTestDataString(numberOfTestStringKeys);

        // Populate node
        var connectionKeys = redisConnections.keySet().toArray(new String[0]);
        var sourceNodeKey = connectionKeys[0];
        var destinationNodeKey = connectionKeys[1];
        var sourceNode = redisConnections.get(sourceNodeKey);
        var destinationNode = redisConnections.get(destinationNodeKey);

        populateRedisWithTestDataHash(testHashItems, sourceNode);
        populateRedisWithTestDataSet(testSetItems, sourceNode);
        populateRedisWithTestDataList(testListItems, sourceNode);
        populateRedisWithTestDataString(testStringItems, sourceNode);

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(testHashItems.keySet());
        allKeys.addAll(testSetItems.keySet());
        allKeys.addAll(testListItems.keySet());
        allKeys.addAll(testStringItems.keySet());


        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        shuffler.copyTuples(sourceNodeKey, destinationNodeKey, allKeys);

        Set<String> destinationResKeys;
        try(Jedis jedis = destinationNode.getResource()){
            destinationResKeys = jedis.keys("*");
        }
        assertEquals(allKeys, destinationResKeys);

        assertCopyResultsHash(testHashItems, destinationNode);
        assertCopyResultsSet(testSetItems, destinationNode);
        assertCopyResultsList(testListItems, destinationNode);
        assertCopyResultsString(testStringItems, destinationNode);
    }

    /**
     * Test Redis shuffler STRING,LIST,HASH,SET copies with missing keys
     */
    @Test
    public void testCopyMixedTuplesWithMissingKeys(){
        final int numberOfTestStringKeys = 100;
        final int numberOfTestListKeys = 100;
        final int numberOfItemsPerList = 20;
        final int numberOfTestSetKeys = 100;
        final int numberOfItemsPerSet = 20;
        final int numberOfTestHashKeys = 100;
        final int numberOfItemsPerHash = 20;

        final int numberOfMissingKeysString = 5;
        final int numberOfMissingKeysList = 15;
        final int numberOfMissingKeysSet = 25;
        final int numberOfMissingKeysHash = 35;

        Map<String, Map<String, String>> testHashItems = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);
        Map<String, Set<String>> testSetItems = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);
        Map<String, List<String>> testListItems = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);
        Map<String, String> testStringItems = generateTestDataString(numberOfTestStringKeys);

        // Populate node
        var connectionKeys = redisConnections.keySet().toArray(new String[0]);
        var sourceNodeKey = connectionKeys[0];
        var destinationNodeKey = connectionKeys[1];
        var sourceNode = redisConnections.get(sourceNodeKey);
        var destinationNode = redisConnections.get(destinationNodeKey);

        populateRedisWithTestDataHash(testHashItems, sourceNode);
        populateRedisWithTestDataSet(testSetItems, sourceNode);
        populateRedisWithTestDataList(testListItems, sourceNode);
        populateRedisWithTestDataString(testStringItems, sourceNode);

        // Add non-existing keys to the copy query
        Set<String> keysWithMissingKeysHash = new HashSet<>(testHashItems.keySet());
        for(int i = 0; i < numberOfMissingKeysHash; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysHash.add(randomKey);
        }

        Set<String> keysWithMissingKeysSet = new HashSet<>(testSetItems.keySet());
        for(int i = 0; i < numberOfMissingKeysSet; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysSet.add(randomKey);
        }

        Set<String> keysWithMissingKeysList = new HashSet<>(testListItems.keySet());
        for(int i = 0; i < numberOfMissingKeysList; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysList.add(randomKey);
        }

        Set<String> keysWithMissingKeysString = new HashSet<>(testStringItems.keySet());
        for(int i = 0; i < numberOfMissingKeysString; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysString.add(randomKey);
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(keysWithMissingKeysHash);
        allKeys.addAll(keysWithMissingKeysSet);
        allKeys.addAll(keysWithMissingKeysList);
        allKeys.addAll(keysWithMissingKeysString);


        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        var copyResult = shuffler.copyTuples(sourceNodeKey, destinationNodeKey, allKeys);

        Set<String> allExistingKeys = new HashSet<>();
        allExistingKeys.addAll(testHashItems.keySet());
        allExistingKeys.addAll(testSetItems.keySet());
        allExistingKeys.addAll(testListItems.keySet());
        allExistingKeys.addAll(testStringItems.keySet());

        Set<String> allMissingKeys = new HashSet<>(allKeys);
        allMissingKeys.removeAll(testHashItems.keySet());
        allMissingKeys.removeAll(testSetItems.keySet());
        allMissingKeys.removeAll(testListItems.keySet());
        allMissingKeys.removeAll(testStringItems.keySet());

        assertEquals(allExistingKeys, copyResult.getProcessedKeys());
        assertEquals(allMissingKeys, copyResult.getFailedKeys());

        Set<String> destinationResKeys;
        try(Jedis jedis = destinationNode.getResource()){
            destinationResKeys = jedis.keys("*");
        }
        assertEquals(allExistingKeys, destinationResKeys);

        assertCopyResultsHash(testHashItems, destinationNode);
        assertCopyResultsSet(testSetItems, destinationNode);
        assertCopyResultsList(testListItems, destinationNode);
        assertCopyResultsString(testStringItems, destinationNode);
    }

    /**
     * Test Redis shuffler STRING,LIST,HASH,SET copies with missing keys
     */
    @Test
    public void testDeleteMixedTuplesWithMissingKeys(){
        final int numberOfTestStringKeys = 100;
        final int numberOfTestListKeys = 100;
        final int numberOfItemsPerList = 20;
        final int numberOfTestSetKeys = 100;
        final int numberOfItemsPerSet = 20;
        final int numberOfTestHashKeys = 100;
        final int numberOfItemsPerHash = 20;

        final int numberOfMissingKeysString = 5;
        final int numberOfMissingKeysList = 15;
        final int numberOfMissingKeysSet = 25;
        final int numberOfMissingKeysHash = 35;

        Map<String, Map<String, String>> testHashItems = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);
        Map<String, Set<String>> testSetItems = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);
        Map<String, List<String>> testListItems = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);
        Map<String, String> testStringItems = generateTestDataString(numberOfTestStringKeys);

        // Populate node
        var connectionKeys = redisConnections.keySet().toArray(new String[0]);
        var nodeKey = connectionKeys[0];
        var node = redisConnections.get(nodeKey);

        populateRedisWithTestDataHash(testHashItems, node);
        populateRedisWithTestDataSet(testSetItems, node);
        populateRedisWithTestDataList(testListItems, node);
        populateRedisWithTestDataString(testStringItems, node);

        // Add non-existing keys to the copy query
        Set<String> keysWithMissingKeysHash = new HashSet<>(testHashItems.keySet());
        for(int i = 0; i < numberOfMissingKeysHash; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysHash.add(randomKey);
        }

        Set<String> keysWithMissingKeysSet = new HashSet<>(testSetItems.keySet());
        for(int i = 0; i < numberOfMissingKeysSet; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysSet.add(randomKey);
        }

        Set<String> keysWithMissingKeysList = new HashSet<>(testListItems.keySet());
        for(int i = 0; i < numberOfMissingKeysList; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysList.add(randomKey);
        }

        Set<String> keysWithMissingKeysString = new HashSet<>(testStringItems.keySet());
        for(int i = 0; i < numberOfMissingKeysString; i++){
            var randomKey = UUID.randomUUID().toString().replace("-", "");
            keysWithMissingKeysString.add(randomKey);
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(keysWithMissingKeysHash);
        allKeys.addAll(keysWithMissingKeysSet);
        allKeys.addAll(keysWithMissingKeysList);
        allKeys.addAll(keysWithMissingKeysString);

        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        var deleteResult = shuffler.deleteTuples(nodeKey, allKeys);

        Set<String> allExistingKeys = new HashSet<>();
        allExistingKeys.addAll(testHashItems.keySet());
        allExistingKeys.addAll(testSetItems.keySet());
        allExistingKeys.addAll(testListItems.keySet());
        allExistingKeys.addAll(testStringItems.keySet());

        Set<String> allMissingKeys = new HashSet<>(allKeys);
        allMissingKeys.removeAll(testHashItems.keySet());
        allMissingKeys.removeAll(testSetItems.keySet());
        allMissingKeys.removeAll(testListItems.keySet());
        allMissingKeys.removeAll(testStringItems.keySet());

        assertEquals(allExistingKeys, deleteResult.getProcessedKeys());
        assertEquals(allMissingKeys, deleteResult.getFailedKeys());

        Set<String> retrievedKeys;
        try(Jedis jedis = node.getResource()){
            retrievedKeys = jedis.keys("*");
        }

        Set<String> emptySet = new HashSet<>();
        assertEquals(emptySet, retrievedKeys);
    }

    /**
     * Test data removal from the data nodes
     */
    @Test
    public void testFlushAllData(){
        final int numberOfTestStringKeys = 512;
        final int numberOfTestListKeys = 256;
        final int numberOfItemsPerList = 20;
        final int numberOfTestSetKeys = 100;
        final int numberOfItemsPerSet = 42;
        final int numberOfTestHashKeys = 100;
        final int numberOfItemsPerHash = 20;

        Map<String, Map<String, String>> testHashItems_node1 = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);
        Map<String, Set<String>> testSetItems_node1 = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);
        Map<String, List<String>> testListItems_node1 = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);
        Map<String, String> testStringItems_node1 = generateTestDataString(numberOfTestStringKeys);

        Map<String, Map<String, String>> testHashItems_node2 = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);
        Map<String, Set<String>> testSetItems_node2 = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);
        Map<String, List<String>> testListItems_node2 = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);
        Map<String, String> testStringItems_node2 = generateTestDataString(numberOfTestStringKeys);

        var nodeIds = redisConnections.keySet().toArray(new String[0]);
        // Populate node 1
        var nodeId_node1 = nodeIds[0];
        var pool_node1 = redisConnections.get(nodeId_node1);
        populateRedisWithTestDataHash(testHashItems_node1, pool_node1);
        populateRedisWithTestDataSet(testSetItems_node1, pool_node1);
        populateRedisWithTestDataList(testListItems_node1, pool_node1);
        populateRedisWithTestDataString(testStringItems_node1, pool_node1);

        // Populate node 2
        var nodeId_node2 = nodeIds[1];
        var pool_node2 = redisConnections.get(nodeId_node2);
        populateRedisWithTestDataHash(testHashItems_node2, pool_node2);
        populateRedisWithTestDataSet(testSetItems_node2, pool_node2);
        populateRedisWithTestDataList(testListItems_node2, pool_node2);
        populateRedisWithTestDataString(testStringItems_node2, pool_node2);
        // Check the keys were created in the database
        try(Jedis jedis = pool_node1.getResource()){
            long expectedNKeys = testHashItems_node1.size()
                    + testSetItems_node1.size()
                    + testListItems_node1.size()
                    + testStringItems_node1.size();
            assertEquals(expectedNKeys , jedis.dbSize().longValue());
        }

        try(Jedis jedis = pool_node2.getResource()){
            long expectedNKeys = testHashItems_node2.size()
                    + testSetItems_node2.size()
                    + testListItems_node2.size()
                    + testStringItems_node2.size();
            assertEquals(expectedNKeys , jedis.dbSize().longValue());
        }

        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);
        shuffler.flushData();

        try(Jedis jedis = pool_node1.getResource()){
            assertEquals(0 , jedis.dbSize().longValue());
        }

        try(Jedis jedis = pool_node2.getResource()){
            assertEquals(0 , jedis.dbSize().longValue());
        }
    }

    /**
     * Test dumping the nodes data into a file and restoring it back
     */
    @Test
    public void testSerialization(){
        final int numberOfTestStringKeys = 512;
        final int numberOfTestListKeys = 256;
        final int numberOfItemsPerList = 20;
        final int numberOfTestSetKeys = 100;
        final int numberOfItemsPerSet = 42;
        final int numberOfTestHashKeys = 100;
        final int numberOfItemsPerHash = 20;

        Map<String, Map<String, String>> testHashItems_node1 = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);
        Map<String, Set<String>> testSetItems_node1 = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);
        Map<String, List<String>> testListItems_node1 = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);
        Map<String, String> testStringItems_node1 = generateTestDataString(numberOfTestStringKeys);

        Map<String, Map<String, String>> testHashItems_node2 = generateTestDataHash(numberOfTestHashKeys, numberOfItemsPerHash);
        Map<String, Set<String>> testSetItems_node2 = generateTestDataSet(numberOfTestSetKeys, numberOfItemsPerSet);
        Map<String, List<String>> testListItems_node2 = generateTestDataList(numberOfTestListKeys, numberOfItemsPerList);
        Map<String, String> testStringItems_node2 = generateTestDataString(numberOfTestStringKeys);

        var nodeIds = redisConnections.keySet().toArray(new String[0]);
        // Populate node 1
        String nodeId_node1 = nodeIds[0];
        var pool_node1 = redisConnections.get(nodeId_node1);
        populateRedisWithTestDataHash(testHashItems_node1, pool_node1);
        populateRedisWithTestDataSet(testSetItems_node1, pool_node1);
        populateRedisWithTestDataList(testListItems_node1, pool_node1);
        populateRedisWithTestDataString(testStringItems_node1, pool_node1);

        // Populate node 2
        String nodeId_node2 = nodeIds[1];
        var pool_node2 = redisConnections.get(nodeId_node2);
        populateRedisWithTestDataHash(testHashItems_node2, pool_node2);
        populateRedisWithTestDataSet(testSetItems_node2, pool_node2);
        populateRedisWithTestDataList(testListItems_node2, pool_node2);
        populateRedisWithTestDataString(testStringItems_node2, pool_node2);

        // Test shuffler
        IDataShuffler shuffler = new RedisDataShuffler(testRedisNodes);

        Path workingDir = FileSystems.getDefault().getPath(".").toAbsolutePath();
        String testFileName = UUID.randomUUID().toString().replace("-", "") + ".rdmp";
        String pathToTestFile = workingDir.resolve(testFileName).normalize().toString();
        File testFile = new File(pathToTestFile);
        try {
            shuffler.dumpToFile(pathToTestFile, true);
            assertTrue(testFile.exists());
            instancesCleanup(); // Remove all data from the nodes
            shuffler.restoreFromFile(pathToTestFile);

            assertCopyResultsHash(testHashItems_node1, pool_node1);
            assertCopyResultsSet(testSetItems_node1, pool_node1);
            assertCopyResultsList(testListItems_node1, pool_node1);
            assertCopyResultsString(testStringItems_node1, pool_node1);

            assertCopyResultsHash(testHashItems_node2, pool_node2);
            assertCopyResultsSet(testSetItems_node2, pool_node2);
            assertCopyResultsList(testListItems_node2, pool_node2);
            assertCopyResultsString(testStringItems_node2, pool_node2);

        } catch (IOException e) {
            fail(e);
        }
        finally {
            if(testFile.exists()){
                testFile.delete();
            }
        }
    }

//region Common assertions
    private void assertCopyResultsHash(Map<String, Map<String, String>> testItems, JedisPool node) {
        for (Map.Entry<String, Map<String, String>> testItem : testItems.entrySet()) {
            Map<String, String> copiedResult = null;
            try (Jedis jedis = node.getResource()) {
                copiedResult = jedis.hgetAll(testItem.getKey());
            }
            assertEquals(testItem.getValue(), copiedResult);
        }
    }

    private void assertCopyResultsSet(Map<String, Set<String>> testItems, JedisPool node) {
        for (Map.Entry<String, Set<String>> testItem : testItems.entrySet()) {
            Set<String> copiedResult = null;
            try (Jedis jedis = node.getResource()) {
                copiedResult = jedis.smembers(testItem.getKey());
            }
            assertEquals(testItem.getValue(), copiedResult);
        }
    }

    private void assertCopyResultsList(Map<String, List<String>> testItems, JedisPool node) {
        for (Map.Entry<String, List<String>> testItem : testItems.entrySet()) {
            List<String> copiedResult = null;
            try (Jedis jedis = node.getResource()) {
                copiedResult = jedis.lrange(testItem.getKey(), 0, -1);
            }
            assertEquals(testItem.getValue(), copiedResult);
        }
    }

    private void assertCopyResultsString(Map<String, String> testItems, JedisPool node) {
        for (Map.Entry<String, String> testItem : testItems.entrySet()) {
            String copiedResult = null;
            try (Jedis jedis = node.getResource()) {
                copiedResult = jedis.get(testItem.getKey());
            }
            assertEquals(testItem.getValue(), copiedResult);
        }
    }
//endregion

//region Test data generation methods
    private void populateRedisWithTestDataHash(Map<String, Map<String, String>> testItems, JedisPool sourceNode) {
        try(Jedis jedis = sourceNode.getResource()){
            try(Pipeline p = jedis.pipelined()){
                for(var item: testItems.entrySet()){
                    p.hset(item.getKey(), item.getValue());
                }
                p.sync();
            }
        }
    }

    private void populateRedisWithTestDataSet(Map<String, Set<String>> testItems, JedisPool sourceNode) {
        try(Jedis jedis = sourceNode.getResource()){
            try(Pipeline p = jedis.pipelined()){
                for(var item: testItems.entrySet()){
                    p.sadd(item.getKey(), item.getValue().toArray(new String[0]));
                }
                p.sync();
            }
        }
    }

    private void populateRedisWithTestDataString(Map<String, String> testItems, JedisPool sourceNode) {
        try(Jedis jedis = sourceNode.getResource()){
            try(Pipeline p = jedis.pipelined()){
                for(var item: testItems.entrySet()){
                    p.set(item.getKey(), item.getValue());
                }
                p.sync();
            }
        }
    }

    private void populateRedisWithTestDataList(Map<String, List<String>> testItems, JedisPool sourceNode) {
        try(Jedis jedis = sourceNode.getResource()){
            try(Pipeline p = jedis.pipelined()){
                for(var item: testItems.entrySet()){
                    p.rpush(item.getKey(), item.getValue().toArray(new String[0]));
                }
                p.sync();
            }
        }
    }

    @NotNull
    private Map<String, Map<String, String>> generateTestDataHash(int numberOfTestHashKeys, int numberOfItemsPerHash) {
        // Key : <Hash-key : Hash-value>
        Map<String, Map<String, String>> testItems = new HashMap<>();
        // Generate hashes
        for(int i = 0; i < numberOfTestHashKeys; i ++){
            String uniqueKey = UUID.randomUUID().toString().replace("-", "");
            Map<String, String> testHashKeyValues = new HashMap<>();
            for(int j = 0; j < numberOfItemsPerHash; j++){
                String testHashField = UUID.randomUUID().toString().replace("-", "");
                String testHashValue = UUID.randomUUID().toString();
                testHashKeyValues.put(testHashField, testHashValue);
            }
            testItems.put(uniqueKey, testHashKeyValues);
        }
        return testItems;
    }

    @NotNull
    private Map<String, Set<String>> generateTestDataSet(int numberOfTestSetKeys, int numberOfItemsPerSet) {
        // Key : <Set>
        Map<String, Set<String>> testItems = new HashMap<>();
        // Generate sets
        for(int i = 0; i < numberOfTestSetKeys; i ++){
            String uniqueKey = UUID.randomUUID().toString().replace("-", "");
            Set<String> testSetValues = new HashSet<>();
            for(int j = 0; j < numberOfItemsPerSet; j++){
                String testSetValue = UUID.randomUUID().toString();
                testSetValues.add(testSetValue);
            }
            testItems.put(uniqueKey, testSetValues);
        }
        return testItems;
    }

    @NotNull
    private Map<String, List<String>> generateTestDataList(int numberOfTestListKeys, int numberOfItemsPerList) {
        // Key : <Set>
        Map<String, List<String>> testItems = new HashMap<>();
        // Generate sets
        for(int i = 0; i < numberOfTestListKeys; i ++){
            String uniqueKey = UUID.randomUUID().toString().replace("-", "");
            List<String> testListValues = new ArrayList<>();
            for(int j = 0; j < numberOfItemsPerList; j++){
                String testSetValue = UUID.randomUUID().toString();
                testListValues.add(testSetValue);
            }
            testItems.put(uniqueKey, testListValues);
        }
        return testItems;
    }

    @NotNull
    private Map<String, String> generateTestDataString(int numberOfTestSetKeys) {
        // Key : <String>
        Map<String, String> testItems = new HashMap<>();
        // Generate sets
        for (int i = 0; i < numberOfTestSetKeys; i++) {
            String uniqueKey = UUID.randomUUID().toString().replace("-", "");
            String testValue = UUID.randomUUID().toString();
            testItems.put(uniqueKey, testValue);
        }
        return testItems;
    }
//endregion
}
