package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.server.tcp.handler.EchoBenchmarkHandler;
import dev.jcri.mdde.registry.server.tcp.handler.MddeBenchmarkHandler;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkDecoder;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.CharsetUtil;

import java.nio.ByteOrder;

public class TCPBenchmarkPipelineInitializer extends ChannelInitializer<SocketChannel> {

    private boolean _isEchoServer = false;

    public TCPBenchmarkPipelineInitializer(boolean isEchoServer){
        super();
        _isEchoServer = isEchoServer;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // Protocol:
        // Asymmetric
        // # In
        //------------------
        // [2 bytes] n - size of the payload in bytes
        //------------------
        // [1 byte] operation code
        //------------------
        // [n bytes] payload serialized into String
        //------------------
        // # Out
        //------------------
        // [2 bytes] n - size of the payload in bytes
        //------------------
        // [1 byte] - operation state result
        //------------------
        // [n bytes] payload serialized into String
        //------------------
        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2, 0, 2));
        socketChannel.pipeline().addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 2,0, false));
        socketChannel.pipeline().addLast(new BenchmarkEncoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast(new BenchmarkDecoder(CharsetUtil.UTF_8));
        if(_isEchoServer){
            socketChannel.pipeline().addLast(new EchoBenchmarkHandler());
        }
        else{
            socketChannel.pipeline().addLast(new MddeBenchmarkHandler());
        }


    }
}
