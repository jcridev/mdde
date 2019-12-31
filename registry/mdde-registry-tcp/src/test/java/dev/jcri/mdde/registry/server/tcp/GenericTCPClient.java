package dev.jcri.mdde.registry.server.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

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
            /*
            try(OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8")){
                osw.write(line, 0, line.length());
            }
            */
            var in = new Scanner(socket.getInputStream());
            var outin = socket.getOutputStream();
            DataOutputStream out=new DataOutputStream(outin);

            //byte[] pingArr = "ping".getBytes();
            //out.write(pingArr, 0, pingArr.length);
            //out.flush();
            //System.out.println(in.nextLine());
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
            //bArr[4] = 1;

            try {
                out.write(bArr, 0, bArr.length);
                out.flush();
                System.out.println(in.nextLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a)
    {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
}
