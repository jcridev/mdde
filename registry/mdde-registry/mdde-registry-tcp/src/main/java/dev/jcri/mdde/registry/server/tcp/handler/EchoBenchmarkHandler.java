package dev.jcri.mdde.registry.server.tcp.handler;

import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerIn;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkResultCodes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EchoBenchmarkHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger logger = LogManager.getLogger(EchoBenchmarkHandler.class);

    private BenchmarkContainerIn _lastReceivedMessage = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            _lastReceivedMessage = (BenchmarkContainerIn) msg;
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            if(_lastReceivedMessage != null){
                ctx.write(processCommand(_lastReceivedMessage));
                ctx.flush();
            }
        }
        catch (Exception ex){
            logger.error(ex.getMessage(), ex);
        }
        finally {
            _lastReceivedMessage = null;
            super.channelReadComplete(ctx);
        }
    }

    protected BenchmarkContainerOut processCommand(BenchmarkContainerIn command){
        // TODO: Benchmark processing
        if(command.getParameter() != null && command.getParameter().size() > 0){
            logger.trace(String.format("Got command arg: %s", new String(command.getParameter().get(0))));
        }

        return new BenchmarkContainerOut(BenchmarkResultCodes.OK, command.getParameter());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
