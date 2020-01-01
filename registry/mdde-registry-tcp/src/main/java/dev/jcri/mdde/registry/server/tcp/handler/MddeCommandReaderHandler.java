package dev.jcri.mdde.registry.server.tcp.handler;

import dev.jcri.mdde.registry.server.tcp.CommandProcessorSingleton;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class MddeCommandReaderHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger logger = LogManager.getLogger(MddeCommandReaderHandler.class);

    private String _lastReceivedMessage = null;
    private boolean _isEcho;
    public MddeCommandReaderHandler(){
        super();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String received = (String) msg;
            _lastReceivedMessage = received;
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
                if (_lastReceivedMessage != null && !_lastReceivedMessage.isEmpty()) {
                    logger.trace("Channel read complete: {}; Length: {}", ctx.channel().remoteAddress(), _lastReceivedMessage.length());
                } else {
                    logger.trace("Channel read complete: {}; Empty message.", ctx.channel().remoteAddress());
                }
            }
            ctx.write(processCommand(_lastReceivedMessage));
            ctx.flush();
        }
        catch (Exception ex){
            logger.error(ex);
        }
        finally {
            _lastReceivedMessage = null;
            super.channelReadComplete(ctx);
        }
    }

    /**
     * Perform actual command processing by the MDDE registry logic
     * @param command Incoming command text
     * @return Textual response to be sent back to the client
     */
    protected String processCommand(String command){
        var cmd = CommandProcessorSingleton.getDefaultInstance().getCommandProcessor();
        return cmd.processIncomingStatement(command);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause);
        ctx.close();
    }
}
