package dev.jcri.mdde.registry.server.nettytcp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * As simple as it gets TCP client defining generic interactions with the server for testing
 */
public class GenericTCPClient {

    public void testLinesInSequence(final int port, final List<String> lines, final int withDelay) throws Exception {
        testLinesInSequence("localhost", port, lines, withDelay);
    }

    public void testLinesInSequence(final String host,
                     final int port, List<String> lines, final int withDelay) throws Exception {
        try (var socket = new Socket(host, port)) {
            var in = new Scanner(socket.getInputStream());
            var out = new PrintWriter(socket.getOutputStream(), true);
            lines.stream().forEach(line -> {
                try {
                    Thread.sleep(withDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                out.println(line);
            });
        }
    }
}
