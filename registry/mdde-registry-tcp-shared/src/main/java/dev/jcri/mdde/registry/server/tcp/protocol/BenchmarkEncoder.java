package dev.jcri.mdde.registry.server.tcp.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

public class BenchmarkEncoder extends MessageToMessageEncoder<BenchmarkContainerOut> {

    private final Charset _charset;

    public BenchmarkEncoder(){
        this(Charset.defaultCharset());
    }

    public BenchmarkEncoder(Charset charset){
        Objects.requireNonNull(charset);
        this._charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          BenchmarkContainerOut msg,
                          List<Object> out)
            throws Exception {
        if (msg == null) {
            return;
        }
        byte[] commandCodeByte = {msg.getResult().value()};
        ByteBuf commandCode = Unpooled.wrappedBuffer(commandCodeByte);
        ByteBuf payload = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg.getReturnValue()), _charset);

        out.add(Unpooled.wrappedBuffer(commandCode, payload));
    }
}
