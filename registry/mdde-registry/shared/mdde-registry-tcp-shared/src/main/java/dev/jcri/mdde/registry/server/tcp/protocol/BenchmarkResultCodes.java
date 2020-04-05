package dev.jcri.mdde.registry.server.tcp.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Possible benchmark return codes
 */
public enum BenchmarkResultCodes  {
    OK((byte)0),
    ERROR((byte)1);

    private final byte _code;

    private BenchmarkResultCodes(byte code){
        _code = code;
    }

    public byte value(){
        return _code;
    }

    private static Map<Byte, BenchmarkResultCodes> _commandsMap =
            Arrays.stream(BenchmarkResultCodes.values()).collect(Collectors.toMap(e -> e._code, e -> e));

    public static BenchmarkResultCodes getValidCode(byte code) throws NoSuchElementException {
        BenchmarkResultCodes command = _commandsMap.get(code);
        if(command == null){
            throw new NoSuchElementException(Byte.toString(code));
        }
        return command;
    }
}
