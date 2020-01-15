package dev.jcri.mdde.registry.server.tcp.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Possible benchmark operations
 */
public enum BenchmarkOperationCodes {
    LOCATE_TUPLE((byte)0, "Locate tuple"),
    RELEASE_CAPACITY((byte)1, "Release capacity");

    private final byte _code;
    private final String _description;

    private BenchmarkOperationCodes(byte code, String description){
        _code = code;
        _description = description;
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


    @Override
    public String toString() {
        return _description;
    }
}
