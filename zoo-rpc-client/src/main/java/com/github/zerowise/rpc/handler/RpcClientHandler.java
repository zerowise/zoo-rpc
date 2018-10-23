package com.github.zerowise.rpc.handler;

import com.github.zerowise.rpc.SyncResultListener;
import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.common.RpcResult;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 ** @createtime : 2018/10/23 2:18 PM
 **/
@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> implements SyncResultListener {

    private ConcurrentHashMap<String, RpcResult> results = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        onMessageRead(ctx, msg);
    }

    @Override
    public RpcResult onMessageWrite(String messageId) {
        RpcResult rpcResult = new RpcResult();
        results.putIfAbsent(messageId, rpcResult);
        return rpcResult;
    }

    @Override
    public void onMessageRead(ChannelHandlerContext ctx, RpcResponse msg) {
        RpcResult rpcResult = results.remove(msg.getMessageId());
        if (rpcResult != null) {
            rpcResult.onResult(msg);
        }
    }
}
