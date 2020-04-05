package dev.jcri.mdde.registry.shared.benchmark.ycsb.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * Stats collector params of MddeArgs.STATS_COLLECTOR
 */
public enum EYcsbStatsCollector {
    /**
     * Local file based stats collector
     */
    TYPE_STATS_LOCAL_VALUE("loc");

    private final String _value;

    EYcsbStatsCollector(final String flag){
        _value = flag;
    }

    @Override
    public String toString() {
        return _value;
    }

    private static final Map<String, EYcsbStatsCollector> _lookup = new HashMap<>();
    static
    {
        for(EYcsbStatsCollector env : EYcsbStatsCollector.values())
        {
            _lookup.put(env.toString(), env);
        }
    }

    /**
     * Get enum by value
     * @param value String value
     * @return YcsbStatsCollector for a valid value, null otherwise
     */
    public static EYcsbStatsCollector get(String value)
    {
        return _lookup.get(value);
    }
}
