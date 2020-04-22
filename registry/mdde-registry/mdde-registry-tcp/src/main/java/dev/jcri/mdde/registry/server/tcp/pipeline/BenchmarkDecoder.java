package dev.jcri.mdde.registry.server.tcp.pipeline;

import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerIn;
import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkOperationCodes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.LinkedList;
import java.util.List;

/**
 * Decoder responsible for unmarshalling of incoming benchmark commands.
 */
public class BenchmarkDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte operationType = msg.readByte();
        byte numberOfArguments = msg.readByte();

        if (numberOfArguments == 0) {
            out.add(new BenchmarkContainerIn(BenchmarkOperationCodes.getValidCode(operationType), null));
        } else {
            List<byte[]> arguments = new LinkedList<>();
            for (int i = 0; i < numberOfArguments; i++) {
                byte argumentLength = msg.readByte();
                byte[] tmpBytes = new byte[argumentLength];
                msg.readBytes(tmpBytes);
                arguments.add(tmpBytes);
            }
            out.add(new BenchmarkContainerIn(BenchmarkOperationCodes.getValidCode(operationType), arguments));
        }
    }
}