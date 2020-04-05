package dev.jcri.mdde.registry.clinet.tcp.benchmark.pipeline;

import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerIn;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkOperationCodes;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkResultCodes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.LinkedList;
import java.util.List;

public class BenchmarkResponseDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        byte resultCode = byteBuf.readByte();
        byte numberOfArguments = byteBuf.readByte();

        if (numberOfArguments == 0) {
            out.add(new BenchmarkContainerOut(BenchmarkResultCodes.getValidCode(resultCode), null));
        } else {
            List<byte[]> values = new LinkedList<>();
            for (int i = 0; i < numberOfArguments; i++) {
                byte valuesLength = byteBuf.readByte();
                byte[] tmpBytes = new byte[valuesLength];
                byteBuf.readBytes(tmpBytes);
                values.add(tmpBytes);
            }
            out.add(new BenchmarkContainerOut(BenchmarkResultCodes.getValidCode(resultCode), values));
        }
    }
}
