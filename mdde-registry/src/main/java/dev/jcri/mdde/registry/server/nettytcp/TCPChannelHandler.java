package dev.jcri.mdde.registry.server.nettytcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TCPChannelHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LogManager.getLogger(TCPChannelHandler.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("Channel active: {}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        logger.debug("Channel: {}, message: {}", ctx.channel().remoteAddress(), s);
        ctx.channel().writeAndFlush("test\n");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("Channel: {} inactive", ctx.channel().remoteAddress());
    }
}
