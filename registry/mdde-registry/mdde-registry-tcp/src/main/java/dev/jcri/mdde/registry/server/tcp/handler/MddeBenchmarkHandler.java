package dev.jcri.mdde.registry.server.tcp.handler;

import dev.jcri.mdde.registry.clinet.tcp.benchmark.commands.CommandArgsConverter;
import dev.jcri.mdde.registry.server.tcp.BenchmarkRunnerSingleton;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerIn;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkResultCodes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler for the TCP calls by the benchmark runner.
 */
public class MddeBenchmarkHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger logger = LogManager.getLogger(MddeBenchmarkHandler.class);

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
            if(_lastReceivedMessage != null) {
                if (logger.isTraceEnabled()){
                    logger.trace(_lastReceivedMessage.toString());
                }
                ctx.write(processCommand(_lastReceivedMessage));
            }
            ctx.flush();
        }
        catch (Exception ex){
            logger.error("channelReadComplete error", ex);
        }
        finally {
            _lastReceivedMessage = null;
            super.channelReadComplete(ctx);
        }
    }

    /**
     * Execute the received and decoded benchmark command.
     * @param command Decoded benchmark command.
     * @return Response container for the benchmark client.
     */
    protected BenchmarkContainerOut processCommand(BenchmarkContainerIn command){
        var commandTag = command.getOperation();
        try {
            logger.trace("Benchmark command is executed in thread: {}.", Thread.currentThread().getId());
            var runner = BenchmarkRunnerSingleton.getDefaultInstance().getRunner();

            BenchmarkContainerOut response = null;
            switch (commandTag) {
                case LOCATE_TUPLE:
                    logger.trace("Start to process tuple location command");
                    var runnerArg = CommandArgsConverter.unmarshalLocateTuple(command);
                    var result = runner.getTupleLocation(runnerArg);
                    response = CommandArgsConverter.marshalTupleLocatorResponse(BenchmarkResultCodes.OK, result);
                    logger.trace("Finished to process tuple location command");
                    break;
                case RELEASE_CAPACITY:
                    logger.trace("Start to process release capacity command");
                    var nodeIdArg = CommandArgsConverter.unmarshalString(command);
                    runner.notifyNodeAccessFinished(nodeIdArg);
                    response = CommandArgsConverter.marshalStringResponse(BenchmarkResultCodes.OK, "ok");
                    logger.trace("Finished to process release capacity command");
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unhandled benchmark command command '%s'",
                                    command.getOperation().toString()));
            }

            return response;
        }
        catch (Exception e){
            logger.error("processCommand error", e);
            return new BenchmarkContainerOut(BenchmarkResultCodes.ERROR, null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
