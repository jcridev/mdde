package dev.jcri.mdde.registry.exceptions;

/**
 * Catalog of error codes returned by registry exceptions
 */
public enum EErrorCode {
    /**
     * Value: 0
     * Standard Java exception (likely a code level issue, rather than a logical issue of the registry)
     */
    RUNTIME_ERROR(0x00),
    /**
     * Value: 9999
     * Unable to retrieve error type (indicates code level issues within the registry)
     */
    UNSPECIFIED_ERROR(0x0270F),

    /**
     * Value: 2100
     */
    DUPLICATE_ENTITY(0x0834),
    /**
     * Value: 2101
     */
    UNKNOWN_ENTITY_ID(0x0835),

    /**
     * Value: 2201
     */
    UNKNOWN_COMMAND(0x0899),
    /**
     * Value: 2202
     */
    MISSING_COMMAND_ARGUMENT(0x089A),

    /**
     * Value: 2300
     */
    READ_OPERATION_ERROR(0x08FC),
    /**
     * Value: 2301
     */
    WRITE_OPERATION_ERROR(0x08FD),

    /**
     * Value: 2350
     */
    RESPONSE_SERIALIZATION_ERROR(0x092E),

    /**
     * Value: 2500
     */
    REGISTRY_MODE_ALREADY_SET(0x09C4),
    /**
     * Value: 2501
     */
    INCORRECT_REGISTRY_MODE_FOR_OPERATION(0x09C5),


    /**
     * Value:2601
     */
    DUPLICATE_FRAGMENT_REPLICATION(0x0A29),
    /**
     * Value:2602
     */
    LOCAL_FRAGMENT_REPLICATION(0x0A2A),
    /**
     * Value:2603
     */
    NON_COLOCATED_FRAGMENT_FORMATION(0x0A2B),
    /**
     * Value:2604
     */
    UNIQUE_FRAGMENT_REMOVAL(0x0A2C),
    /**
     * Value:2605
     */
    SEED_NON_EMPTY_REGISTRY(0x0A2D),

    /**
     * Value: 3100
     */
    DATA_KEY_NOT_FOUND(0x0C1C),

    /**
     * Value: 4100
     */
    CTRL_ILLEGAL_COMMAND_ARGUMENT(0x01004),
    /**
     * Value: 4101
     */
    CTRL_MALFORMED_COMMAND_STATEMENT(0x01005)
    ;


    private final int _errorCode;

    private EErrorCode(int errorCode){
        _errorCode = errorCode;
    }

    public int getErrorCode() {
        return _errorCode;
    }

    public String getErrorCodeBase16(){
        return Integer.toString(_errorCode, 16);
    }

    @Override
    public String toString() {
        return getErrorCodeBase16();
    }

    /**
     * Try get enum value by int error code
     * @param errorCode int error code
     * @return EErrorCode value or null if can't find value for the corresponding code
     */
    public static EErrorCode getValue(Integer errorCode)
    {
        EErrorCode[] As = EErrorCode.values();
        for (EErrorCode a : As) {
            if (a.getErrorCode() == errorCode)
                return a;
        }
        return null;
    }
}
