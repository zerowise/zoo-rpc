package com.github.zerowise.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 ** @createtime : 2018/10/225:57 PM
 **/
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    private final Class<?> clazz;
    private static final int MAX_FRAME_LENGTH = 0XFFFFFF;

    public RpcDecoder(Class<?> clazz) {
        super(MAX_FRAME_LENGTH, 0, 4, 0, 4);
        this.clazz = clazz;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
        byte[] bytes = new byte[byteBuf.readableBytes()];
        return SerializationUtil.deserialize(bytes, clazz);
    }
}
