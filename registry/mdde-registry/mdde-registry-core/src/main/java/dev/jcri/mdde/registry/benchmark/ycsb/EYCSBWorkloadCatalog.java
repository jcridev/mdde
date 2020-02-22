package dev.jcri.mdde.registry.benchmark.ycsb;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public enum EYCSBWorkloadCatalog {
    READ_10000("read10000", "/benchmark/ycsb/workload_read_10000");

    private final String _tag;
    private final String _resourceFile;
    private final String _baseFileName;

    EYCSBWorkloadCatalog(String tag, String resourceFile){
        if(resourceFile == null || resourceFile.isBlank()){
            throw new IllegalArgumentException("Workload resource name can't be empty");
        }
        _resourceFile = resourceFile;
        _baseFileName= Paths.get(_resourceFile).getFileName().toString();
        if(_baseFileName.isBlank()){
            throw new IllegalArgumentException("Workload resource name is not correct");
        }

        _tag = tag;
    }

    public String getResourceFileName(){
        return _resourceFile;
    }

    public String getResourceBaseFileName(){
        return _baseFileName;
    }

    public String getTag(){
        return _tag;
    }

    @Override
    public String toString() {
        return _resourceFile;
    }

    private static Map<String, EYCSBWorkloadCatalog> _commandsMap = Arrays.stream(EYCSBWorkloadCatalog.values())
            .collect(Collectors.toMap(e -> e._tag, e -> e));

    public static EYCSBWorkloadCatalog getWorkloadByTag(String tag) throws NoSuchElementException {
        if(tag == null || tag.isEmpty()){
            throw new IllegalArgumentException("tag can't be null or empty");
        }
        EYCSBWorkloadCatalog command = _commandsMap.get(tag);
        if(command == null){
            throw new NoSuchElementException(tag);
        }
        return command;
    }
}
