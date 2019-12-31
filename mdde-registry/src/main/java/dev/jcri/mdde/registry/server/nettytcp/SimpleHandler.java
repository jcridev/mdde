package dev.jcri.mdde.registry.server.nettytcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class SimpleHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LogManager.getLogger(TCPChannelHandler.class);

    private int _totalLenghth = 0;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        String received = (String) msg;//inBuffer.toString(CharsetUtil.UTF_8);

        var receivedLength = received.length();
        _totalLenghth += receivedLength;
        var msgDbg = MessageFormat.format("Channel read: {0}; string payload length: {1};",
                ctx.channel().remoteAddress(), receivedLength);
        logger.debug(msgDbg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel read complete: {}; Length: {}", ctx.channel().remoteAddress(), _totalLenghth);
        //ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        ctx.write("got message\n");
        ctx.flush();
        super.channelReadComplete(ctx);
        _totalLenghth = 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause);
        ctx.close();
    }
}
