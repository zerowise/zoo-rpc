package com.github.zerowise.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 ** @createtime : 2018/10/226:55 PM
 **/
public class RpcEncoder extends MessageToByteEncoder<Object> {

    private final Class clazz;
    public RpcEncoder(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(clazz.isAssignableFrom(msg.getClass())){
            byte[] bytes = SerializationUtil.serialize(msg);
            out.writeInt(bytes.length).writeBytes(bytes);
        }
    }
}
