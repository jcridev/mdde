package dev.jcri.mdde.registry.benchmark.ycsb;

import java.nio.file.Paths;

public enum EWorkloadCatalog {
    READ_10000("benchmark/ycsb/workload_read_10000");

    private final String _resourceFile;
    private final String _baseFileName;

    EWorkloadCatalog(String resourceFile){
        if(resourceFile == null || resourceFile.isBlank()){
            throw new IllegalArgumentException("Workload resource name can't be empty");
        }
        _resourceFile = resourceFile;
        _baseFileName= Paths.get(_resourceFile).getFileName().toString();
        if(_baseFileName.isBlank()){
            throw new IllegalArgumentException("Workload resource name is not correct");
        }
    }

    public String getResourceFileName(){
        return _resourceFile;
    }

    public String getResourceBaseFileName(){
        return _baseFileName;
    }

    @Override
    public String toString() {
        return _resourceFile;
    }
}
