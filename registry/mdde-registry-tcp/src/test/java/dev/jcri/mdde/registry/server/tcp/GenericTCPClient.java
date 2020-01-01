package dev.jcri.mdde.registry.server.tcp;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * As simple as it gets TCP client defining generic interactions with the server for testing
 */
public class GenericTCPClient {

    public void testLinesInSequence(final int port, final String line, final int withDelay) throws Exception {
        testLinesInSequence("localhost", port, line, withDelay);
    }

    public void testLinesInSequence(final String host,
                     final int port, String line, final int withDelay) throws Exception {
        try (var socket = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            try {
                Thread.sleep(withDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("Send payload of length: %d", line.length()));
            byte[] message = line.getBytes(StandardCharsets.UTF_8);
            byte[] length = intToByteArray(message.length);
            byte[] bArr = new byte[4 + message.length];
            System.arraycopy(message, 0, bArr, 4, message.length);
            System.arraycopy(length, 0, bArr, 0, length.length);

            out.write(bArr, 0, bArr.length);
            out.flush();
            int lenFieldLength = 4; // Length of the length part of the response frame
            byte[] responseLength = new byte[lenFieldLength];
            for(int i = 0; i < lenFieldLength; i++){
                responseLength[i] = in.readByte();
            }
            int parsedLength = byteArrayToInt(responseLength);
            byte[] responsePayload = new byte[parsedLength];

            boolean gotFullResponse = false;
            int bytesRead = 0;
            while(!gotFullResponse){
                bytesRead += in.read(responsePayload);
                if (bytesRead == parsedLength)
                {
                    gotFullResponse = true;
                }
            }
            var result = new String(responsePayload, StandardCharsets.UTF_8);
            assertEquals(line, result);
        }
    }

    private int byteArrayToInt(byte[] bytes)
    {
        if(bytes.length != 4){
            throw new IllegalArgumentException(String.format("Expected a byte array of length 4 but received: %d", bytes.length));
        }

        return bytes[3] & 0xFF |
                (bytes[2] & 0xFF) << 8 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[0] & 0xFF) << 24;
    }

    private byte[] intToByteArray(int number)
    {
        return new byte[] {
                (byte) ((number >> 24) & 0xFF),
                (byte) ((number >> 16) & 0xFF),
                (byte) ((number >> 8) & 0xFF),
                (byte) ( number & 0xFF)
        };
    }
}
