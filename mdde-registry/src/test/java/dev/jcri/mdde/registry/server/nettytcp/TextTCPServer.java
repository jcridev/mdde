package dev.jcri.mdde.registry.server.nettytcp;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

public class TextTCPServer {

    @Test
    public void startServerExchangeMessages(){
        Listener tcpListener = new Listener();
        final int testPort = 8095;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    tcpListener.start(testPort);
                } catch (InterruptedException e) {
                    fail("Failed to start the server", e);
                }
            }
        };
        try {
            new Thread(r).start();
            var randStrings = new ArrayList<String>();
            for(int i = 0; i < 10; i++){
                randStrings.add(UUID.randomUUID().toString());
            }
            var gTcpClient = new GenericTCPClient();
            try {
                gTcpClient.testLinesInSequence(testPort, randStrings, 1000);
            } catch (Exception e) {
                fail("Failed server communication");
            }
        }
        finally {
            tcpListener.stop();
        }
    }
}
