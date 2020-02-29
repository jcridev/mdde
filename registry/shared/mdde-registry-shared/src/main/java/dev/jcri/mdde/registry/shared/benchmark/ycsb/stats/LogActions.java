package dev.jcri.mdde.registry.shared.benchmark.ycsb.stats;

public enum LogActions {
    READ("r");

    private final String _actionFlag;

    LogActions(final String flag){
        _actionFlag = flag;
    }

    @Override
    public String toString() {
        return _actionFlag;
    }
}
