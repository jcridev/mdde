package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.server.tcp.handler.EchoBenchmarkHandler;
import dev.jcri.mdde.registry.server.tcp.handler.MddeBenchmarkHandler;
import dev.jcri.mdde.registry.server.tcp.pipeline.BenchmarkDecoder;
import dev.jcri.mdde.registry.server.tcp.pipeline.BenchmarkEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

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
        // [2 bytes] n - total size of the payload in bytes (including operation code and argument length bytes)
        //------------------
        // [1 byte] operation code
        //------------------
        // [1 byte] a - number of arguments
        //------------------
        // [1 bytes] a[0]_n - length of a[0] argument
        //------------------
        // [a[0]_n bytes] payload argument
        //------------------
        // [1 bytes] a[1]_n - length of a[1] argument
        //------------------
        // [a[1]_n bytes] payload argument
        //------------------
        // ...
        //------------------
        // # Out
        //------------------
        // [2 bytes] n - size of the payload in bytes (including result code and value length bytes)
        //------------------
        // [1 byte] - operation state result
        //------------------
        // [1 byte] v - number of returned values
        //------------------
        // [1 bytes] v[0]_n - length of v[0] value
        //------------------
        // [v[0]_n bytes] payload value
        //------------------
        // [1 bytes] V[1]_n - length of v[1] value
        //------------------
        // [v[1]_n bytes] payload value
        //------------------
        // ...
        //------------------
        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2, 0, 2));
        socketChannel.pipeline().addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 2,0, false));
        socketChannel.pipeline().addLast(new BenchmarkEncoder());
        socketChannel.pipeline().addLast(new BenchmarkDecoder());
        if(_isEchoServer){
            socketChannel.pipeline().addLast(new EchoBenchmarkHandler());
        }
        else{
            socketChannel.pipeline().addLast(new MddeBenchmarkHandler());
        }
    }
}
