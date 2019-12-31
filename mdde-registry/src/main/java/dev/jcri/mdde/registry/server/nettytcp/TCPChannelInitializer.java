package dev.jcri.mdde.registry.server.nettytcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TCPChannelInitializer extends ChannelInitializer<SocketChannel> {
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new StringEncoder());
        //socketChannel.pipeline().addLast(new LineBasedFrameDecoder(50000, true, true));
        socketChannel.pipeline().addLast(new StringDecoder());
        //socketChannel.pipeline().addLast(new TCPChannelHandler());
        //socketChannel.pipeline().addLast(new TestHandler());
        socketChannel.pipeline().addLast(new SimpleHandler());
    }
}
