package dev.jcri.mdde.registry.server.tcp;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public class TestTCPServer {

    @Test
    public void startServerExchangeMessages() throws InterruptedException {
        TestListener tcpListener = new TestListener();
        final int testPort = 8095;
        var numOfClients = 1;
        var numOfMessagesPerClient = 1;
        var payloadSizePerClient = 8000;

        Runnable rServer = () -> {
            try {
                tcpListener.startEcho(testPort);
            } catch (InterruptedException e) {
                Assertions.fail("Failed to start the server", e);
            }
        };
        var randStrings = new ArrayList<String>();
        for(int i = 0; i < numOfMessagesPerClient; i++){
            try {
                randStrings.add(genRandomString(payloadSizePerClient, 0));
            } catch (InterruptedException e) {
                fail(e);
                return;
            }
        }
        System.out.println("Random strings generated");
        Runnable rClient = () -> {
            var gTcpClient = new GenericTCPClient();
            for(var randLine: randStrings){
                try {
                    gTcpClient.testLinesInSequence(testPort, randLine, 1000);
                } catch (Exception e) {
                    Assertions.fail("Failed server communication");
                }
            }
        };
        var serverThread = new Thread(rServer);
        try {
            serverThread.start();
            List<Thread> threads = new ArrayList<>();
            for(int i = 0; i < numOfClients; i ++){
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

//region Test Listener
    /**
     * Echo server
     */
    private class TestListener extends Listener {
        /**
         * Start the Listener as echo
         */
        public void startEcho(int port) throws InterruptedException {
            start(port, true);
        }
    }


//region Generate test payload
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz\\+-=\")(_][";
    static SecureRandom rnd = new SecureRandom();

    private String genRandomString(int len, int lineBreaksEvery) throws InterruptedException {
        if(len > 2000){
            final int binLength = 10000;
            int bins = len / binLength;
            int rem = len - binLength*bins;
            ExecutorService es = Executors.newCachedThreadPool();
            var tasks = new ArrayList<GenRandomString>();
            for(int i=0;i<bins;i++) {
                var newTask = new GenRandomString(AB, binLength, lineBreaksEvery);
                tasks.add(newTask);
                es.execute(newTask);
            }

            if(rem > 0){
                var newTask = new GenRandomString(AB, rem, lineBreaksEvery);
                tasks.add(newTask);
                es.execute(newTask);
            }

            es.shutdown();
            boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
            if(finished){
                StringBuilder sb = new StringBuilder(len);
                tasks.forEach(item -> {
                    sb.append(item.getResult());
                });
                return sb.toString();
            }
            else {
                throw new InterruptedException("Failed to generate strings");
            }
        }
        else{
            var randomStringGenerator = new GenRandomString(AB, len, lineBreaksEvery);
            randomStringGenerator.run();
            return randomStringGenerator.getResult();
        }
    }

    public class GenRandomString implements Runnable {
        private final String _alpahabet;
        private final int _len;
        private final int _lineBreaksEvery;
        private String _result = null;

        public GenRandomString(String alphabet, int length, int lineBreaksEvery){
            _alpahabet = alphabet;
            _len = length;
            _lineBreaksEvery = lineBreaksEvery;
        }

        public String getResult(){
            return _result;
        }

        private void makeRandomString(){
            StringBuilder sb = new StringBuilder( _len );
            for( int i = 0; i < _len; i++ ) {
                if(_lineBreaksEvery != 0 && i % _lineBreaksEvery == 0){
                    sb.append("\n");
                }
                else {
                    sb.append(_alpahabet.charAt(rnd.nextInt(_alpahabet.length())));
                }
            }
            _result = sb.toString();
        }

        public void run(){
            makeRandomString();
        }
    }
//endregion
}
