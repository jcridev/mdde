package dev.jcri.mdde.registry.shared.benchmark.ycsb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class MDDEClientConfigurationBase {
    public final static String FILE_EXTENSION = "yml";
    protected final static Charset configurationCharset = StandardCharsets.UTF_8;

    private final ObjectMapper _mapper;

    protected MDDEClientConfigurationBase(){
        _mapper = new ObjectMapper(new YAMLFactory());
    }

    protected ObjectMapper getMapper(){
        return _mapper;
    }
}
