package dev.jcri.mdde.registry.benchmark.ycsb.cli;

import java.util.*;
import java.util.stream.Collectors;

public enum EKnownYCSBModifiers {
    RUNTIME_MS("RunTime(ms)", ModifierType.is_integer),
    THROUGHPUT("Throughput(ops/sec)", ModifierType.is_double),
    LATENCY_AVG("AverageLatency(ms)", ModifierType.is_double),
    LATENCY_MIN("MinLatency(ms)", ModifierType.is_integer),
    LATENCY_MAX("MaxLatency(ms)", ModifierType.is_integer),
    RETURN_OK("Return=OK", ModifierType.is_integer),
    RETURN_ERROR("Return=ERROR", ModifierType.is_integer),
    OPERATIONS("Operations", ModifierType.is_integer);


    private final String _tag;
    private final ModifierType _expectedType;

    private EKnownYCSBModifiers(String tag, ModifierType expectedType){
        Objects.requireNonNull(tag);
        if(tag.isEmpty() || tag.isBlank()){
            throw new IllegalArgumentException("tag can't be empty");
        }
        _tag = tag;
        _expectedType = expectedType;
    }

    private static Map<String, EKnownYCSBModifiers> _commandsMap =
            Arrays.stream(EKnownYCSBModifiers.values()).collect(Collectors.toMap(e -> e._tag, e -> e));

    public String getTag(){
        return _tag;
    }

    public ModifierType getExpectedType(){
        return _expectedType;
    }

    /**
     *
     * @param tag
     * @return A modifier, if known. Otherwise null.
     */
    public static EKnownYCSBModifiers getCommandTag(String tag) {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        return _commandsMap.get(tag);
    }

    enum ModifierType  {
        is_integer,
        is_double;
    }
}
