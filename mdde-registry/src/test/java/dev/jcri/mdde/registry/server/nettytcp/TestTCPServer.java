package dev.jcri.mdde.registry.server.nettytcp;


import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class TestTCPServer {

    @Test
    public void startServerExchangeMessages() throws InterruptedException {
        Listener tcpListener = new Listener();
        final int testPort = 8095;

        Runnable rServer = new Runnable() {
            public void run() {
                try {
                    tcpListener.start(testPort);
                } catch (InterruptedException e) {
                    fail("Failed to start the server", e);
                }
            }
        };
        Runnable rClient = new Runnable() {
            public void run() {
                var randStrings = new ArrayList<String>();
                for(int i = 0; i < 10; i++){
                    randStrings.add(genRandomString(1000000, 0));
                }
                var gTcpClient = new GenericTCPClient();
                for(var randLine: randStrings){
                    try {
                        gTcpClient.testLinesInSequence(testPort, randLine, 1000);
                    } catch (Exception e) {
                        fail("Failed server communication");
                    }
                }
            }
        };
        var serverThread = new Thread(rServer);
        try {
            serverThread.start();

            List<Thread> threads = new ArrayList<>();
            for(int i = 0; i < 10; i ++){
                var clientThread = new Thread(rClient);
                clientThread.start();
                threads.add(clientThread);
            }

            for (Thread thread : threads) thread.join();
        }
        finally {
            tcpListener.stop();
            serverThread.join();
        }
    }
//region Generate test payload
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz\\+-=\")(_][";
    static SecureRandom rnd = new SecureRandom();

    private String genRandomString(int len, int lineBreaksEvery){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ ) {
            if(lineBreaksEvery != 0 && i % lineBreaksEvery == 0){
                sb.append("\n");
            }
            else {
                sb.append(AB.charAt(rnd.nextInt(AB.length())));
            }
        }
        return sb.toString();
    }
//endregion
}
