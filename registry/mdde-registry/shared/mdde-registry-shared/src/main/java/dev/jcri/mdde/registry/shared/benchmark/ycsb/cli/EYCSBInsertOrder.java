package dev.jcri.mdde.registry.shared.benchmark.ycsb.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * Order of the insertion to the data nodes by YCSB clients for MDDE.
 */
public enum EYCSBInsertOrder {
    /**
     * New data records should be inserted into the data node with the least number of the data records.
     * This is the default insertion order.
     */
    UNIFORM("u"),
    /**
     * Sequential insertion. Insert equal number of data records per node. Nodes for insertion are chosen in sequence.
     * Each node will have total_records/number_of_nodes max data records. New records are inserted into the first
     * till it's full, then second till that full as well, and so on.
     */
    SEQUENTIAL("s");

    private final String _value;

    EYCSBInsertOrder(final String flag){
        _value = flag;
    }

    @Override
    public String toString() {
        return _value;
    }

    private static final Map<String, EYCSBInsertOrder> _lookup = new HashMap<>();
    static
    {
        for(EYCSBInsertOrder env : EYCSBInsertOrder.values())
        {
            _lookup.put(env.toString(), env);
        }
    }

    /**
     * Get enum by value
     * @param value String value
     * @return YcsbStatsCollector for a valid value, null otherwise
     */
    public static EYCSBInsertOrder get(String value)
    {
        return _lookup.get(value);
    }
}