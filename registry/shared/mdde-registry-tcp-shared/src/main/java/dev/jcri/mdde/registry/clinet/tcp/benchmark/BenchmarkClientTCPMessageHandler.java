package dev.jcri.mdde.registry.clinet.tcp.benchmark;

import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BenchmarkClientTCPMessageHandler extends SimpleChannelInboundHandler<BenchmarkContainerOut> {

    private final BlockingQueue<BenchmarkContainerOut> response = new LinkedBlockingQueue<BenchmarkContainerOut>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                BenchmarkContainerOut msg) throws Exception {
        if(msg != null) {
            response.add(msg);
        }
        ctx.fireChannelRead(msg);
    }

    // TODO: Refactor for thread safety
    public BenchmarkContainerOut getResponse()
            throws InterruptedException {
        return response.poll(5000, TimeUnit.MILLISECONDS);
    }
}
