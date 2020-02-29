package dev.jcri.mdde.registry.benchmark.ycsb.stats.result;

public class FragmentBenchmarkStats {
    private final String _fragmentId;
    private int _reads;

    public FragmentBenchmarkStats(String fragmentId, int reads){
        this._fragmentId = fragmentId;
        this._reads = reads;
    }

    public String getFragmentId() {
        return _fragmentId;
    }

    public int getReads() {
        return _reads;
    }

    public void setReads(int value) {
        _reads = value;
    }

    public int incrementReads(){
        return ++_reads;
    }
}
