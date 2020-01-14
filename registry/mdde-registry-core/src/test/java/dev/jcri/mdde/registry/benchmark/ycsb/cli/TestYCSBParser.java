package dev.jcri.mdde.registry.benchmark.ycsb.cli;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestYCSBParser {

    @Test
    public void testParser0(){
        InputStream is = TestYCSBParser.class.getResourceAsStream("/test_ycsb_0.out");
        String testWorkloadOut = new BufferedReader(new InputStreamReader(is)).lines()
                .parallel().collect(Collectors.joining("\n"));

        var ycsbParser = new YCSBOutputParser();
        var res = ycsbParser.parse(testWorkloadOut);
        assertEquals(10110, res.getRuntime());
        assertEquals(98.91196834817013, res.getThroughput());
        assertEquals(0.054989816700611, res.getUpdateLatencyAverage());
        assertEquals(491, res.getUpdateOperations());
    }
}
