package dev.jcri.mdde.registry.clinet.tcp.benchmark;

import dev.jcri.mdde.registry.clinet.tcp.benchmark.commands.CommandArgsConverter;
import dev.jcri.mdde.registry.clinet.tcp.benchmark.pipeline.BenchmarkCommandEncoder;
import dev.jcri.mdde.registry.clinet.tcp.benchmark.pipeline.BenchmarkResponseDecoder;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerIn;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkResultCodes;
import dev.jcri.mdde.registry.shared.benchmark.IMDDEBenchmarkClient;
import dev.jcri.mdde.registry.shared.benchmark.commands.LocateTuple;
import dev.jcri.mdde.registry.shared.benchmark.responses.TupleLocation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.IOException;
import java.nio.ByteOrder;

public class BenchmarkClient implements IMDDEBenchmarkClient {

    private final String _host;
    private final int _port;

    /**
     * Constructor
     * @param host Benchmark TCP interface host
     * @param port Benchmark TCP port
     */
    public BenchmarkClient(String host, int port){
        if(host == null || host.isEmpty()){
            throw new IllegalArgumentException("Host can't be null or empty");
        }
        if(port < 1){
            throw new IllegalArgumentException(String.format("Illegal TCP port value: %d", port));
        }

        _host = host;
        _port = port;
    }


    private EventLoopGroup _clientWorkerGroup = new NioEventLoopGroup();
    private Channel _clientChannel = null;
    private BenchmarkClientTCPMessageHandler _messagingHandler = null;
    /**
     * Open connection
     */
    @Override
    public synchronized void openConnection()
            throws InterruptedException, IOException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(_clientWorkerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        _messagingHandler = new BenchmarkClientTCPMessageHandler();
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2, 0, 2));
                ch.pipeline().addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 2,0, false));
                ch.pipeline().addLast(new BenchmarkCommandEncoder());
                ch.pipeline().addLast(new BenchmarkResponseDecoder());
                ch.pipeline().addLast(_messagingHandler);
            }
        });
        ChannelFuture f = bootstrap.connect(_host, _port).sync();
        if(f.isSuccess()) {
            _clientChannel = f.channel();
        }
        else{
            throw new IOException(String.format("Unable to connect to the server"));
        }
    }

    @Override
    public TupleLocation locateTuple(LocateTuple tupleParam)
            throws InterruptedException {
        if(_clientChannel == null){
            throw new IllegalStateException("The client is not initialized");
        }

        BenchmarkContainerIn marshalledLocateTuple = CommandArgsConverter.marshal(tupleParam);

        ChannelFuture messageFuture = _clientChannel.writeAndFlush(marshalledLocateTuple).sync();
        BenchmarkContainerOut response = _messagingHandler.getResponse();
        if(response.getResult() == BenchmarkResultCodes.OK){
            return CommandArgsConverter.unmarshalTupleLocation(response);
        }
        return null;
    }

    /**
     * Shut down the client
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if(_clientWorkerGroup != null
                && !_clientWorkerGroup.isShutdown()
                && !_clientWorkerGroup.isTerminated()
                && !_clientWorkerGroup.isShuttingDown()){
            _clientWorkerGroup.shutdownGracefully();
        }
    }
}
