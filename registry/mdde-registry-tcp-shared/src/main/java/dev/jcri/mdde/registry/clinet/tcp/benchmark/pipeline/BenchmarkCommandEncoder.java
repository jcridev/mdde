package dev.jcri.mdde.registry.clinet.tcp.benchmark.pipeline;

import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerIn;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class BenchmarkCommandEncoder extends MessageToMessageEncoder<BenchmarkContainerIn> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          BenchmarkContainerIn msg,
                          List<Object> out)
            throws Exception {
        if (msg == null) {
            return;
        }
        byte[][] payload = new byte[2 + msg.numberOfArguments()*2][];
        byte[] commandCodeByte = {msg.getOperation().value()};
        payload[0] = commandCodeByte;

        if(msg.numberOfArguments() == 0){
            byte[] numberOfArgs = {0};
            payload[1] = numberOfArgs;
        }
        else{
            byte[] numberOfArgs = {msg.numberOfArguments()};
            payload[1] = numberOfArgs;
            for(int i = 0; i < msg.getParameter().size(); i = i + 2){
                byte[] cArg = msg.getParameter().get(i);
                byte[] cArgLength = {(byte) cArg.length};
                payload[2 + i] = cArgLength;
                payload[2 + i + 1] = cArg;
            }
        }
        out.add(Unpooled.wrappedBuffer(payload));
    }
}
