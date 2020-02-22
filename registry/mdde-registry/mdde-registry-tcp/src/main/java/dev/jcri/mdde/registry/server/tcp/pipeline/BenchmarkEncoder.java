package dev.jcri.mdde.registry.server.tcp.pipeline;

import dev.jcri.mdde.registry.server.tcp.protocol.BenchmarkContainerOut;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BenchmarkEncoder extends MessageToMessageEncoder<BenchmarkContainerOut> {

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          BenchmarkContainerOut msg,
                          List<Object> out)
            throws Exception {
        if (msg == null) {
            return;
        }
        byte[][] payload = new byte[2 + msg.numberOfValues()*2][];
        byte[] commandCodeByte = {msg.getResult().value()};
        payload[0] = commandCodeByte;

        if(msg.numberOfValues() == 0){
            byte[] numberOfArgs = {0};
            payload[1] = numberOfArgs;
        }
        else{
            byte[] numberOfArgs = {msg.numberOfValues()};
            payload[1] = numberOfArgs;
            for(int i = 0; i < msg.getReturnValue().size(); i = i + 2){
                byte[] cArg = msg.getReturnValue().get(i);
                byte[] cArgLength = {(byte) cArg.length};
                payload[2 + i] = cArgLength;
                payload[2 + i + 1] = cArg;
            }
        }
        out.add(Unpooled.wrappedBuffer(payload));
    }
}
