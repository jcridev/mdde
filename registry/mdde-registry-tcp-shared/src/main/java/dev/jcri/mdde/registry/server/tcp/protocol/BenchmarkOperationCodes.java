package dev.jcri.mdde.registry.server.tcp.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Possible benchmark operations
 */
public enum BenchmarkOperationCodes {
    LOCATETUPLE((byte)0);

    private final byte _code;

    private BenchmarkOperationCodes(byte code){
        _code = code;
    }

    public byte value(){
        return _code;
    }

    private static Map<Byte, BenchmarkOperationCodes> _commandsMap =
            Arrays.stream(BenchmarkOperationCodes.values()).collect(Collectors.toMap(e -> e._code, e -> e));

    public static BenchmarkOperationCodes getValidCode(byte code) throws NoSuchElementException {
        BenchmarkOperationCodes command = _commandsMap.get(code);
        if(command == null){
            throw new NoSuchElementException(Byte.toString(code));
        }
        return command;
    }
}
