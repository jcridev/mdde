package dev.jcri.mdde.registry.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class MddeCommandReaderHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(MddeCommandReaderHandler.class);

    private String lastReceivedMessage = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String received = (String) msg;
            lastReceivedMessage = received;
            if(logger.isDebugEnabled()){
                var receivedLength = received.length();
                var msgDbg = MessageFormat.format("Channel read: {0}; string payload length: {1};",
                        ctx.channel().remoteAddress(), receivedLength);
                logger.trace(msgDbg);
            }
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            if(logger.isTraceEnabled()){
                if (lastReceivedMessage != null && !lastReceivedMessage.isEmpty()) {
                    logger.trace("Channel read complete: {}; Length: {}", ctx.channel().remoteAddress(), lastReceivedMessage.length());
                    ctx.write("got message\n");
                } else {
                    logger.trace("Channel read complete: {}; Empty message.", ctx.channel().remoteAddress());
                }
            }

            var cmd = CommandProcessorSingleton.getDefaultInstance().getCommandProcessor();
            var response = cmd.processIncomingStatement(lastReceivedMessage);

            ctx.write(response);
            ctx.flush();
        }
        catch (Exception ex){
            logger.error(ex);
        }
        finally {
            lastReceivedMessage = null;
            super.channelReadComplete(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause);
        ctx.close();
    }
}
