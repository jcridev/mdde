package dev.jcri.mdde.registry.shared.benchmark.ycsb.cli;

public enum EMddeArgs {
    /**
     * Configuration file path
     */
    CONFIG_FILE("mdde.cfg"),
    /**
     * Stats collector selector
     */
    STATS_COLLECTOR("mdde.st"),
    /**
     * Path to the local directory utilized by a stats collector
     */
    LOCAL_STATS_DIR_PATH("mdde.st.dir"),
    /**
     * Insertion scheme
     */
    INSERT_SCHEME("mdde.ins");


    private final String _actionFlag;

    EMddeArgs(final String flag){
        _actionFlag = flag;
    }

    @Override
    public String toString() {
        return _actionFlag;
    }
}
