package com.github.zerowise.rpc;

import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.common.RpcResult;
import io.netty.channel.ChannelHandlerContext;

/**
 ** @createtime : 2018/10/23 2:55 PM
 **/
public interface ResultListener {

    /**
     *
     * @param messageId
     * @param asyn if true 使用异步 false 同步
     * @return
     */
    RpcResult onMessageWrite(String messageId, boolean asyn);

    void onMessageRead(ChannelHandlerContext ctx, RpcResponse msg);
}
