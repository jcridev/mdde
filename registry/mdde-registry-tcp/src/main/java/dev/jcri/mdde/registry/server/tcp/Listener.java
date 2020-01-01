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

    EventLoopGroup connectionGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * Start the server listener
     * @param port TCP port to listen
     * @throws InterruptedException
     */
    public void start(int port) throws InterruptedException {
        start(port, false);
    }

    /**
     * Start the server listener
     * @param port TCP port to listen
     * @param isEcho True - instead of processing the actual commands, received payload is echoed back to the client
     * @throws InterruptedException
     */
    protected void start(int port, boolean isEcho) throws InterruptedException {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(connectionGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TCPPipelineInitializer(isEcho))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            if(f.isSuccess()){
                logger.info("TCP Server listens on port {}", port);
            }
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            connectionGroup.shutdownGracefully();
        }
    }

    /**
     * Shut down the TCP server
     */
    void stop(){
        workerGroup.shutdownGracefully();
        connectionGroup.shutdownGracefully();
    }
}
