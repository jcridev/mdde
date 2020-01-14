package dev.jcri.mdde.registry.benchmark.ycsb.cli;

import java.util.Objects;
import java.util.function.BiConsumer;


public final class YCSBContainerMapEntry<T> {
    private final EKnownYCSBModifiers _modifier;
    private final BiConsumer<YCSBOutput, T> _func;

    public YCSBContainerMapEntry(EKnownYCSBModifiers modifier, BiConsumer<YCSBOutput, T> setter) {
        Objects.requireNonNull(modifier);
        Objects.requireNonNull(setter);
        this._modifier = modifier;
        this._func = setter;
    }

    public EKnownYCSBModifiers getModifier(){
        return _modifier;
    }

    public BiConsumer<YCSBOutput, T> getSetter(){
        return _func;
    }
}
