package dev.jcri.mdde.registry.clinet.tcp.benchmark.commands;

import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerIn;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkOperationCodes;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkResultCodes;
import dev.jcri.mdde.registry.shared.benchmark.commands.LocateTuple;
import dev.jcri.mdde.registry.shared.benchmark.responses.TupleLocation;

import java.lang.reflect.MalformedParametersException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommandArgsConverter {
    public static final Charset STRING_CHARSET = StandardCharsets.UTF_8;

    public static BenchmarkContainerIn marshal(LocateTuple args){
        List<byte[]> properties = new ArrayList<>();
        properties.add(getBytesFromStringArgument(args.getTupleId()));
        return new BenchmarkContainerIn(BenchmarkOperationCodes.LOCATE_TUPLE, properties);
    }

    public static LocateTuple unmarshalLocateTuple(BenchmarkContainerIn response){
        List<byte[]> properties =response.getParameter();
        if(properties.size() != 1){
            throw new MalformedParametersException(
                    String.format("Expected number of properties is 1, passed %d", properties.size()));
        }
        return new LocateTuple(getStringFromBytesProperty(properties.get(0)));
    }

    public static String unmarshalString(BenchmarkContainerIn response){
        List<byte[]> properties =response.getParameter();
        if(properties.size() != 1){
            throw new MalformedParametersException(
                    String.format("Expected number of properties is 1, passed %d", properties.size()));
        }
        return getStringFromBytesProperty(properties.get(0));
    }

    public static BenchmarkContainerOut marshalTupleLocatorResponse(BenchmarkResultCodes result, TupleLocation args){
        List<byte[]> properties = new ArrayList<>();
        properties.add(getBytesFromStringArgument(args.getNodeId()));
        return new BenchmarkContainerOut(result, properties);
    }

    public static BenchmarkContainerOut marshalStringResponse(BenchmarkResultCodes result, String args){
        List<byte[]> properties = new ArrayList<>();
        properties.add(getBytesFromStringArgument(args));
        return new BenchmarkContainerOut(result, properties);
    }

    public static TupleLocation unmarshalTupleLocation(BenchmarkContainerOut response){
        List<byte[]> properties =response.getReturnValue();
        if(properties.size() != 1){
            throw new MalformedParametersException(
                    String.format("Expected number of properties is 1, passed %d", properties.size()));
        }
        return new TupleLocation(getStringFromBytesProperty(properties.get(0)));
    }

    private static String getStringFromBytesProperty(byte[] property){
        String propSting = new String(property, STRING_CHARSET);
        if(propSting.isEmpty()){
            return null;
        }
        else{
            return propSting;
        }
    }

    private static byte[] getBytesFromStringArgument(String argument){
        if(argument != null){
           return argument.getBytes(STRING_CHARSET);
        }
        else{
            return "".getBytes();
        }
    }
}
