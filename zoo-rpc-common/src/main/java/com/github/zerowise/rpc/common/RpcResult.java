package com.github.zerowise.rpc.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 ** @createtime : 2018/10/2310:51 AM
 **/
public class RpcResult {
    private CompletableFuture completableFuture;

    public Object getResult() throws Throwable {
        completableFuture = new CompletableFuture();
        return completableFuture;
    }

    public void onResult(RpcResponse rpcResponse) {
        if(rpcResponse.getError()!=null){
            completableFuture.completeExceptionally(rpcResponse.getError());
        }else{
            completableFuture.complete(rpcResponse.getResult());
        }
    }

}
