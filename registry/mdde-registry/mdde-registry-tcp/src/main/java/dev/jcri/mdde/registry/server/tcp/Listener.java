package dev.jcri.mdde.registry.server.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Listener {
    private static final Logger logger = LogManager.getLogger(Listener.class);
    // Control API protocol
    private ChannelFuture _controlChannelFuture = null;
    private final EventLoopGroup _cncConnectionGroup = new NioEventLoopGroup();
    private final EventLoopGroup _cncWorkerGroup = new NioEventLoopGroup();
    // Benchmark protocol
    private ChannelFuture _benchmarkChannelFuture;
    private final EventLoopGroup _benchmarkConnectionGroup = new NioEventLoopGroup(256);
    private final EventLoopGroup _benchmarkWorkerGroup = new NioEventLoopGroup(256);

    /**
     * Start the server listener.
     * @param commandPort TCP port to listen incoming registry manipulation commands.
     * @param benchmarkPort TCP port to handle benchmark.
     * @throws InterruptedException
     */
    public void start(int commandPort, int benchmarkPort) throws InterruptedException {
        start(commandPort,benchmarkPort, false);
    }
    /**
     * Start the query and registry control server listener.
     * @param port TCP port to listen.
     * @param isEcho True - instead of processing the actual commands, received payload is echoed back to the client.
     * @throws InterruptedException
     */
    private ChannelFuture startControlServer(int port, boolean isEcho) throws InterruptedException{
        ServerBootstrap b = new ServerBootstrap();
        b.group(_cncConnectionGroup, _cncWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new TCPPipelineInitializer(isEcho))
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        return b.bind(port).sync();
    }

    /**
     * Start the benchmark interface.
     * @param port Benchmark interface port.
     * @return Netty 4 ChannelFuture for the benchmark TCP interface.
     * @throws InterruptedException
     */
    private ChannelFuture startBenchmarkServer(int port, boolean isEcho) throws InterruptedException{
        ServerBootstrap b = new ServerBootstrap();
        b.group(_benchmarkConnectionGroup, _benchmarkWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new TCPBenchmarkPipelineInitializer(isEcho))
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        return b.bind(port).sync();
    }

    /**
     * Start the primary control API and benchmark TCP listeners.
     * @param portControl Port number used for control API.
     * @param portBenchmark Port number used for benchmark.
     * @param isEcho If True - instead of processing the incoming calls, echo them back instead (for server tests).
     * @throws InterruptedException Error creating either the control API or the benchmark TCP pipeline.
     */
    protected void start(int portControl, int portBenchmark, boolean isEcho) throws InterruptedException {
        try {
            _controlChannelFuture = startControlServer(portControl, isEcho);
            if(_controlChannelFuture.isSuccess()){
                logger.info("TCP Control Server listens on port {}", portControl);
            }
            else{
                throw new InterruptedException("Unable to start control TCP handler: "
                        + _controlChannelFuture.cause().getMessage());
            }

            _benchmarkChannelFuture = startBenchmarkServer(portBenchmark, isEcho);
            if(_benchmarkChannelFuture.isSuccess()){
                logger.info("TCP Benchmark Server listens on port {}", portBenchmark);
            }
            else{
                throw new InterruptedException("Unable to start benchmark TCP handler: "
                        + _benchmarkChannelFuture.cause().getMessage());
            }

            // TODO: Proper handling of the close future
            _controlChannelFuture.channel().closeFuture().sync();
            _benchmarkChannelFuture.channel().closeFuture().sync();
        } finally {
            _cncWorkerGroup.shutdownGracefully();
            _cncConnectionGroup.shutdownGracefully();

            _benchmarkWorkerGroup.shutdownGracefully();
            _benchmarkConnectionGroup.shutdownGracefully();
        }
    }

    /**
     * Shut down the TCP server
     */
    synchronized void stop(){
        if(_controlChannelFuture != null){
            _controlChannelFuture.channel().close();
            _controlChannelFuture = null;
        }
        if(_benchmarkChannelFuture != null){
            _benchmarkChannelFuture.channel().close();
            _benchmarkChannelFuture = null;
        }
    }
}
