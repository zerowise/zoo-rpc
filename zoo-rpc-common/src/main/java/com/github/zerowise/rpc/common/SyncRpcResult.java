package com.github.zerowise.rpc.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 ** @createtime : 2018/10/24 11:26 AM
 **/
public class SyncRpcResult extends RpcResult {


    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    private Object result;

    private Throwable error;

    @Override
    public Object getResult() throws Throwable {
        try {
            lock.lock();
            condition.await(100, TimeUnit.SECONDS);
            if (error != null) {
                throw error;
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onResult(RpcResponse rpcResponse) {
        try {
            lock.lock();
            this.result = rpcResponse.getResult();
            this.error = rpcResponse.getError();
        } catch (Exception e) {
            this.error = e;
        } finally {
            condition.signal();
            lock.unlock();
        }
    }

}
