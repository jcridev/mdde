package dev.jcri.mdde.registry.benchmark.ycsb.cli;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Known YCSB output lines
 */
public enum EKnownYCSBLines {
    OVERALL("OVERALL",
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.RUNTIME_MS, (obj, arg) -> obj.setRuntime(arg.longValue())),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.THROUGHPUT, YCSBOutput::setThroughput)),
    UPDATE("UPDATE",
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.OPERATIONS, (obj, arg) -> obj.setUpdateOperations(arg.intValue())),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_AVG, YCSBOutput::setUpdateLatencyAverage),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_MAX, (obj, arg) -> obj.setUpdateLatencyMax(arg.intValue())),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_MIN, (obj, arg) -> obj.setUpdateLatencyMin(arg.intValue()))),
    READ("READ",
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.OPERATIONS, (obj, arg) -> obj.setReadOperations(arg.intValue())),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_AVG, YCSBOutput::setReadLatencyAverage),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_MAX, (obj, arg) -> obj.setReadLatencyMax(arg.intValue())),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_MIN, (obj, arg) -> obj.setReadLatencyMin(arg.intValue()))),
    INSERT("INSERT",
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.OPERATIONS, (obj, arg) -> obj.setInsertOperations(arg.intValue())),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_AVG, YCSBOutput::setInsertLatencyAverage),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_MAX, (obj, arg) -> obj.setInsertLatencyMax(arg.intValue())),
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.LATENCY_MIN, (obj, arg) -> obj.setInsertLatencyMin(arg.intValue()))),
    UPDATE_FAILED("UPDATE-FAILED",
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.OPERATIONS, (obj, arg) -> obj.setUpdateOperationsFailed(arg.intValue()))),
    READ_FAILED("READ-FAILED",
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.OPERATIONS, (obj, arg) -> obj.setReadOperationsFailed(arg.intValue()))),
    INSERT_FAILED("INSERT-FAILED",
            new YCSBContainerMapEntry<>(EKnownYCSBModifiers.OPERATIONS, (obj, arg) -> obj.setInsertOperationsFailed(arg.intValue())));


    private final String _mainTag;
    private final Map<String, YCSBContainerMapEntry<Double>> _modifiersMap;

    @SafeVarargs
    private EKnownYCSBLines(String tag, YCSBContainerMapEntry<Double>... modifiers){
        Objects.requireNonNull(tag);
        if(tag.isEmpty() || tag.isBlank()){
            throw new IllegalArgumentException("tag can't be empty");
        }
        _mainTag = tag;
        if(modifiers != null) {
            _modifiersMap = Collections.unmodifiableMap(
                    Arrays.stream(modifiers).collect(Collectors.toMap(e -> e.getModifier().getTag(), e -> e)));
        }
        else {
            _modifiersMap = Collections.unmodifiableMap(new HashMap<>());
        }
    }

    private static Map<String, EKnownYCSBLines> _commandsMap =
            Arrays.stream(EKnownYCSBLines.values()).collect(Collectors.toMap(e -> e._mainTag, e -> e));

    public String getTag(){
        return _mainTag;
    }

    public Collection<YCSBContainerMapEntry<Double>> getModifiers(){
        return _modifiersMap.values();
    }

    public YCSBContainerMapEntry<Double> tryToGetModifier(String tag) {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        return _modifiersMap.get(tag);
    }

    /**
     *
     * @param tag
     * @return  A known key, if known. Otherwise null.
     */
    public static EKnownYCSBLines getCommandTag(String tag) {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        return _commandsMap.get(tag);
    }



}
