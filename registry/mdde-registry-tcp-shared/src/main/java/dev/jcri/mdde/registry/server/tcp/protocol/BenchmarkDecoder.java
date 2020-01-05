package dev.jcri.mdde.registry.server.tcp.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

public class BenchmarkDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Charset _charset;

    public BenchmarkDecoder(){
        this(Charset.defaultCharset());
    }

    public BenchmarkDecoder(Charset charset){
        Objects.requireNonNull(charset);
        this._charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte operationType = msg.readByte();
        String params = msg.toString(_charset);

        out.add(new BenchmarkContainerIn(BenchmarkOperationCodes.getValidCode(operationType), params));
    }
}