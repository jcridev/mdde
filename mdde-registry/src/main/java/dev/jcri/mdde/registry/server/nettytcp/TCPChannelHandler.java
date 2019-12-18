package dev.jcri.mdde.registry.server.nettytcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringWriter;
import java.text.MessageFormat;

public class TCPChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(TCPChannelHandler.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("Channel active: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel read complete: {}; Lenghth: {}", ctx.channel().remoteAddress(), _totalLenghth);
        ctx.write("got message\n");
        ctx.flush();
        super.channelReadComplete(ctx);
        _totalLenghth = 0;
    }

    private int _totalLenghth = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object  msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            StringWriter strWriter = new StringWriter();
            while (in.isReadable()) {
                strWriter.append((char) in.readByte());
                strWriter.flush();
            }
            var receivedString = strWriter.toString();
            var receivedLength = receivedString.length();
            _totalLenghth += receivedLength;
            var msgDbg = MessageFormat.format("Channel read: {0}; string payload length: {1};",
                            ctx.channel().remoteAddress(), receivedLength);
            logger.debug(msgDbg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("Channel: {} inactive", ctx.channel().remoteAddress());
    }
}
