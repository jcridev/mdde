package dev.jcri.mdde.registry.utility;

import java.nio.ByteBuffer;

public class ByteTools {
    /**
     * Convert byte array of 8 bytes to Long
     * @param bytes Array of 8 bytes (big endian)
     * @return Long value
     */
    public static long byteArrayToLong(byte[] bytes)
    {
        if(bytes.length != 8){
            throw new IllegalArgumentException(String.format("Expected a byte array of length 8 but received: %d",
                    bytes.length));
        }
        return ByteBuffer.wrap(bytes).getLong();
    }

    /**
     * Convert Short to byte array
     * @param number Short value
     * @return Array of 8 bytes (big endian)
     */
    public static byte[] longToByteArray(long number)
    {
        ByteBuffer buf = ByteBuffer.allocate(8);
        return buf.putLong(number).array();
    }

    /**
     * Convert byte array of 4 bytes to Integer
     * @param bytes Array of 4 bytes (big endian)
     * @return Integer value
     */
    public static int byteArrayToInt(byte[] bytes)
    {
        if(bytes.length != 4){
            throw new IllegalArgumentException(String.format("Expected a byte array of length 4 but received: %d",
                    bytes.length));
        }
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * Convert Integer to byte array
     * @param number Integer value
     * @return Array of 4 bytes (big endian)
     */
    public static byte[] intToByteArray(int number)
    {
        ByteBuffer buf = ByteBuffer.allocate(4);
        return buf.putInt(number).array();
    }

    /**
     * Convert byte array of 4 bytes to Short
     * @param bytes Array of 4 bytes (big endian)
     * @return Short value
     */
    public static short byteArrayToShort(byte[] bytes)
    {
        if(bytes.length != 2){
            throw new IllegalArgumentException(String.format("Expected a byte array of length 2 but received: %d",
                    bytes.length));
        }
        return ByteBuffer.wrap(bytes).getShort();
    }

    /**
     * Convert Short to byte array
     * @param number Short value
     * @return Array of 2 bytes (big endian)
     */
    public static byte[] shortToByteArray(short number)
    {
        ByteBuffer buf = ByteBuffer.allocate(2);
        return buf.putShort(number).array();
    }
}
