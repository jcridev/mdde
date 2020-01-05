package dev.jcri.mdde.registry.server.tcp;

import dev.jcri.mdde.registry.server.tcp.handler.EchoReaderHandler;
import dev.jcri.mdde.registry.server.tcp.handler.MddeCommandReaderHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.nio.ByteOrder;

public class TCPPipelineInitializer extends ChannelInitializer<SocketChannel> {
    private boolean _isEchoServer = false;

    public TCPPipelineInitializer(){
        super();
    }

    public TCPPipelineInitializer(boolean isEchoServer){
        super();
        _isEchoServer = isEchoServer;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // Protocol:
        // Symmetric
        //------------------
        // [4 bytes] n - size of the payload in bytes
        //------------------
        // [n bytes] payload, JSON string encoded as UTF-8
        //------------------
        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        socketChannel.pipeline().addLast(new LengthFieldPrepender(ByteOrder.BIG_ENDIAN, 4,0, false));
        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
        if(_isEchoServer)
        {
            socketChannel.pipeline().addLast(new EchoReaderHandler());
        }
        else{
            socketChannel.pipeline().addLast(new MddeCommandReaderHandler());
        }
    }
}
