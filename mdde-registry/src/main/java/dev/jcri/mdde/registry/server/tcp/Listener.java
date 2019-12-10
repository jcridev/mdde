package dev.jcri.mdde.registry.server.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Listener {
    private CancellationToken _globalCancellationToken;
    private int _socketPort;
    private InetAddress _netInterface = null;
    private ListenerThread _listenerThread = null;
    private int _maxCConnections;

    public Listener(int maxCConnections, int port, InetAddress netInt){
        if(maxCConnections < 1){
            throw new IllegalArgumentException("Maximum number of connections must be at least 1");
        }
        if(port < 0){
            throw new IllegalArgumentException("Port number can't be negative");
        }
        _maxCConnections = maxCConnections;
        _socketPort = port;
        _netInterface =netInt;
    }

    public Listener(int maxCConnections, int port){
        this(maxCConnections, port, null);
    }

    ReentrantLock _lockStartStop = new ReentrantLock();

    public void Start() throws IOException {
        _globalCancellationToken = new CancellationToken();
        _lockStartStop.lock();
        try {
            ServerSocket server = new ServerSocket(_socketPort, 50, _netInterface);
            _listenerThread = new ListenerThread(server, _maxCConnections, _globalCancellationToken);
            _listenerThread.start();
        }
        finally {
            _lockStartStop.unlock();
        }
    }

    public void Stop() throws IOException{
        _lockStartStop.lock();
        try {
            if(_listenerThread != null){
                _globalCancellationToken.cancel();
                // TODO: Wait till thread actually shuts down
            }
        }
        finally {
            _listenerThread = null;
            _lockStartStop.unlock();
        }
    }

    private static class ListenerThread extends Thread {
        private ServerSocket _server = null;
        private ExecutorService _threadPool = null;
        private CancellationToken _cancellationToken;

        public ListenerThread(ServerSocket server, int maxCConnections, CancellationToken cancellationToken) {
            if(server == null){
                throw new NullPointerException("Socket server can't be null");
            }
            if(cancellationToken == null){
                throw new NullPointerException("Cancellation token must be supplied");
            }
            _cancellationToken = cancellationToken;
            this._server = server;
             _threadPool = Executors.newFixedThreadPool(maxCConnections);
        }

        public ServerSocket getServer(){
            return _server;
        }

        public CancellationToken getCancellationToken(){
            return _cancellationToken;
        }

        public void run() {
            while (!_cancellationToken.getCancelled()) {
                try {
                    _threadPool.execute(new Listener.ClientHandler(_server.accept()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                _server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean serverIsDown(){
            return _server == null || _server.isClosed();
        }
    }

    private static class ClientHandler implements Runnable {
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Accept messages from this client
                while (true) {
                    String input = in.nextLine();
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                // TODO: Connection closed
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}
