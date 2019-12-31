package dev.jcri.mdde.registry.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class MddeCommandReaderHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(TCPChannelHandler.class);

    private String lastReceivedMessage = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String received = (String) msg;
            lastReceivedMessage = received;
            var receivedLength = received.length();
            var msgDbg = MessageFormat.format("Channel read: {0}; string payload length: {1};",
                    ctx.channel().remoteAddress(), receivedLength);
            logger.trace(msgDbg);
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            if (lastReceivedMessage != null && !lastReceivedMessage.isEmpty()) {
                logger.trace("Channel read complete: {}; Length: {}", ctx.channel().remoteAddress(), lastReceivedMessage.length());
                ctx.write("got message\n");
            } else {
                logger.trace("Channel read complete: {}; Empty message.", ctx.channel().remoteAddress());
            }
            ctx.flush();
        }
        finally {
            lastReceivedMessage = null;
            super.channelReadComplete(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
