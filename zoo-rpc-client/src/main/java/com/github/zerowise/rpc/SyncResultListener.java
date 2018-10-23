package com.github.zerowise.rpc;

import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.common.RpcResult;
import io.netty.channel.ChannelHandlerContext;

/**
 ** @createtime : 2018/10/23 2:55 PM
 **/
public interface SyncResultListener {

    RpcResult onMessageWrite(String messageId);

    void onMessageRead(ChannelHandlerContext ctx, RpcResponse msg);
}
